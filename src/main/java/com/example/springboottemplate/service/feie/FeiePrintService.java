package com.example.springboottemplate.service.feie;

import com.example.springboottemplate.config.FeieProperties;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.entity.MallOrderItem;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StorePrinter;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.MallOrderItemMapper;
import com.example.springboottemplate.mapper.MallOrderMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.StorePrinterMapper;
import com.example.springboottemplate.utils.StoreAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FeiePrintService {

    public static final int PRINT_PENDING = 0;
    public static final int PRINT_SUCCESS = 1;
    public static final int PRINT_FAILED = 2;
    public static final int PRINT_SKIPPED = 3;

    private static final Set<Integer> REPRINTABLE = Set.of(1, 3, 4);

    @Autowired
    private FeieClient feieClient;
    @Autowired
    private FeieProperties feieProperties;
    @Autowired
    private StorePrinterMapper storePrinterMapper;
    @Autowired
    private MallOrderMapper mallOrderMapper;
    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;

    @Async("wxShippingExecutor")
    public void printAfterPaidAsync(String orderNo) {
        try {
            printOrder(orderNo, false);
        } catch (Exception e) {
            log.error("飞鹅自动打印异常 orderNo={}", orderNo, e);
            mallOrderMapper.updatePrintSync(orderNo, PRINT_FAILED, cut(e.getMessage()), new Date());
        }
    }

    public String printOrder(String orderNo, boolean force) {
        MallOrder order = mallOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!force && order.getPrintStatus() != null && order.getPrintStatus() == PRINT_SUCCESS) {
            return "已打印";
        }
        StorePrinter printer = storePrinterMapper.selectByStoreId(order.getStoreId());
        if (printer == null || !StringUtils.hasText(printer.getPrinterSn())) {
            mallOrderMapper.updatePrintSync(orderNo, PRINT_SKIPPED, "未绑定打印机", new Date());
            return "未绑定打印机";
        }
        if (!force && (printer.getPrintEnabled() == null || printer.getPrintEnabled() != 1)) {
            mallOrderMapper.updatePrintSync(orderNo, PRINT_SKIPPED, "已关闭自动打印", new Date());
            return "已关闭自动打印";
        }
        if (!feieProperties.configured()) {
            mallOrderMapper.updatePrintSync(orderNo, PRINT_SKIPPED, "平台飞鹅密钥未配置", new Date());
            if (force) {
                throw new BusinessException("平台飞鹅密钥未配置，请在 yml 填写 feie.ukey");
            }
            return "平台飞鹅密钥未配置";
        }
        Store store = storeMapper.selectByStoreId(order.getStoreId());
        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        String content = FeieTicketBuilder.orderTicket(store, order, items, printer);
        int copies = printer.getCopies() == null ? 1 : printer.getCopies();
        FeieClient.FeieResult result = feieClient.printMsg(printer.getPrinterSn().trim(), content, copies);
        if (!result.ok()) {
            mallOrderMapper.updatePrintSync(orderNo, PRINT_FAILED, cut(result.display()), new Date());
            if (force) {
                throw new BusinessException("打印失败: " + result.display());
            }
            return result.display();
        }
        mallOrderMapper.updatePrintSync(orderNo, PRINT_SUCCESS, cut(result.display()), new Date());
        return "已发送到打印机";
    }

    public String reprint(String orderNo, HttpServletRequest request) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException("订单号不能为空");
        }
        StoreAuthHelper.Operator operator = storeAuthHelper.requireMerchantOrAdmin(request);
        MallOrder order = mallOrderMapper.selectByOrderNo(orderNo.trim());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        storeAuthHelper.assertCanWriteStore(operator, order.getStoreId());
        Integer payStatus = order.getPayStatus();
        if (payStatus == null || !REPRINTABLE.contains(payStatus)) {
            throw new BusinessException("仅已支付订单可补打");
        }
        return printOrder(order.getOrderNo(), true);
    }

    public String testPrint(Long storeId) {
        StorePrinter printer = storePrinterMapper.selectByStoreId(storeId);
        if (printer == null || !StringUtils.hasText(printer.getPrinterSn())) {
            throw new BusinessException("请先保存打印机编号和识别码");
        }
        feieClient.requireConfigured();
        Store store = storeMapper.selectByStoreId(storeId);
        String storeName = store == null ? null : store.getStoreName();
        boolean voice = printer.getVoiceEnabled() == null || printer.getVoiceEnabled() == 1;
        FeieClient.FeieResult result = feieClient.printMsg(
                printer.getPrinterSn().trim(),
                FeieTicketBuilder.testTicket(storeName, voice),
                1);
        if (!result.ok()) {
            throw new BusinessException("测试打印失败: " + result.display());
        }
        return result.display();
    }

    public String queryStatus(Long storeId) {
        StorePrinter printer = storePrinterMapper.selectByStoreId(storeId);
        if (printer == null || !StringUtils.hasText(printer.getPrinterSn())) {
            throw new BusinessException("请先保存打印机编号");
        }
        feieClient.requireConfigured();
        FeieClient.FeieResult result = feieClient.queryStatus(printer.getPrinterSn().trim());
        String text = result.ok() ? result.display() : ("查询失败: " + result.display());
        printer.setLastStatus(cut(text));
        storePrinterMapper.updateStorePrinter(printer);
        if (!result.ok()) {
            throw new BusinessException(text);
        }
        return text;
    }

    private static String cut(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() <= 250 ? msg : msg.substring(0, 250);
    }
}
