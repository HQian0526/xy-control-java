package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.config.FeieProperties;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.StorePrinterSaveRequest;
import com.example.springboottemplate.dto.StorePrinterVO;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.StorePrinter;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.StorePrinterMapper;
import com.example.springboottemplate.service.StorePrinterService;
import com.example.springboottemplate.service.feie.FeieClient;
import com.example.springboottemplate.service.feie.FeiePrintService;
import com.example.springboottemplate.utils.StoreAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class StorePrinterServiceimpl implements StorePrinterService {

    @Autowired
    private StorePrinterMapper storePrinterMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private StoreAuthHelper storeAuthHelper;
    @Autowired
    private FeieProperties feieProperties;
    @Autowired
    private FeieClient feieClient;
    @Autowired
    private FeiePrintService feiePrintService;

    @Override
    public Response getByStoreId(Long storeId, HttpServletRequest request) {
        storeAuthHelper.requireAdmin(request);
        if (storeId == null) {
            throw new BusinessException("请选择店铺");
        }
        Store store = storeMapper.selectByStoreId(storeId);
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }
        return Response.success(toVo(store, storePrinterMapper.selectByStoreId(storeId)));
    }

    @Override
    public Response list(HttpServletRequest request) {
        storeAuthHelper.requireAdmin(request);
        Store query = new Store();
        query.setDeleted(0);
        @SuppressWarnings("unchecked")
        List<Store> stores = storeMapper.findStore(query);
        List<StorePrinter> printers = storePrinterMapper.findStorePrinter(new StorePrinter());
        Map<Long, StorePrinter> byStore = new HashMap<>();
        if (printers != null) {
            for (StorePrinter item : printers) {
                if (item != null && item.getStoreId() != null) {
                    byStore.put(item.getStoreId(), item);
                }
            }
        }
        List<StorePrinterVO> list = new ArrayList<>();
        if (stores != null) {
            for (Store store : stores) {
                if (store == null || store.getStoreId() == null) {
                    continue;
                }
                list.add(toVo(store, byStore.get(store.getStoreId())));
            }
        }
        return Response.success(list);
    }

    @Override
    public Response save(StorePrinterSaveRequest body, HttpServletRequest request) {
        StoreAuthHelper.Operator operator = storeAuthHelper.requireAdmin(request);
        if (body == null || body.getStoreId() == null) {
            throw new BusinessException("请选择店铺");
        }
        Store store = storeMapper.selectByStoreId(body.getStoreId());
        if (store == null) {
            throw new BusinessException("店铺不存在");
        }

        String sn = trimToNull(body.getPrinterSn());
        String incomingKey = trimToNull(body.getPrinterKey());
        StorePrinter existing = storePrinterMapper.selectByStoreId(body.getStoreId());
        String oldSn = existing == null ? null : trimToNull(existing.getPrinterSn());
        String keepKey = existing == null ? null : existing.getPrinterKey();
        String key = incomingKey != null ? incomingKey : keepKey;
        if (sn == null) {
            key = null;
        }

        if (sn != null) {
            assertSnUnique(sn, body.getStoreId());
            if (!StringUtils.hasText(key)) {
                throw new BusinessException("请填写打印机识别码 Key");
            }
        }

        int copies = body.getCopies() == null ? 1 : body.getCopies();
        if (copies < 1 || copies > 5) {
            throw new BusinessException("打印联数须为 1～5");
        }

        String bindMsg = syncFeieBind(oldSn, sn, key, store.getStoreName());
        Date now = new Date();
        try {
            if (existing == null) {
                StorePrinter record = StorePrinter.builder()
                        .id(IdWorker.getId())
                        .storeId(body.getStoreId())
                        .printerSn(sn)
                        .printerKey(key)
                        .printerName(trimToNull(body.getPrinterName()))
                        .printEnabled(body.getPrintEnabled() == null ? 1 : body.getPrintEnabled())
                        .voiceEnabled(body.getVoiceEnabled() == null ? 1 : body.getVoiceEnabled())
                        .copies(copies)
                        .lastBindMsg(bindMsg)
                        .createdTime(now)
                        .createdBy(operator.getUsername())
                        .deleted(0)
                        .build();
                storePrinterMapper.addStorePrinter(record);
            } else {
                existing.setPrinterSn(sn);
                existing.setPrinterKey(key);
                existing.setPrinterName(trimToNull(body.getPrinterName()));
                existing.setPrintEnabled(body.getPrintEnabled() == null ? existing.getPrintEnabled() : body.getPrintEnabled());
                existing.setVoiceEnabled(body.getVoiceEnabled() == null ? existing.getVoiceEnabled() : body.getVoiceEnabled());
                existing.setCopies(copies);
                existing.setLastBindMsg(bindMsg);
                existing.setUpdateBy(operator.getUsername());
                storePrinterMapper.updateStorePrinter(existing);
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该店铺已绑定打印机");
        }
        StorePrinter saved = storePrinterMapper.selectByStoreId(body.getStoreId());
        return Response.success(toVo(store, saved));
    }

    @Override
    public Response queryStatus(Long storeId, HttpServletRequest request) {
        storeAuthHelper.requireAdmin(request);
        if (storeId == null) {
            throw new BusinessException("请选择店铺");
        }
        return Response.success(feiePrintService.queryStatus(storeId));
    }

    @Override
    public Response testPrint(Long storeId, HttpServletRequest request) {
        storeAuthHelper.requireAdmin(request);
        if (storeId == null) {
            throw new BusinessException("请选择店铺");
        }
        return Response.success(feiePrintService.testPrint(storeId));
    }

    @Override
    public Response reprint(String orderNo, HttpServletRequest request) {
        return Response.success(feiePrintService.reprint(orderNo, request));
    }

    private String syncFeieBind(String oldSn, String newSn, String key, String storeName) {
        boolean snChanged = !Objects.equals(oldSn, newSn);
        if (!feieProperties.configured()) {
            if (newSn == null) {
                return "已清空，待配置飞鹅密钥后再解绑云端";
            }
            return "已保存，平台飞鹅密钥未配置，认证通过后请再保存一次以绑定";
        }
        if (snChanged && oldSn != null) {
            try {
                feieClient.deletePrinter(oldSn);
            } catch (Exception ignored) {
                // 旧机解绑失败不挡住新机绑定
            }
        }
        if (newSn == null) {
            return "已从飞鹅解绑";
        }
        FeieClient.FeieResult result = feieClient.addPrinter(newSn, key, storeName);
        if (result.ok() || alreadyBound(result)) {
            return "已绑定到飞鹅：" + result.display();
        }
        throw new BusinessException("绑定飞鹅失败: " + result.display());
    }

    private static boolean alreadyBound(FeieClient.FeieResult result) {
        String text = (result.msg() + " " + result.data()).toLowerCase();
        return text.contains("已添加") || text.contains("已经添加") || text.contains("exist");
    }

    private void assertSnUnique(String sn, Long storeId) {
        StorePrinter other = storePrinterMapper.selectAnyBySn(sn);
        if (other != null && !Objects.equals(other.getStoreId(), storeId)) {
            throw new BusinessException("该打印机编号已绑定其他店铺");
        }
    }

    private StorePrinterVO toVo(Store store, StorePrinter cfg) {
        StorePrinterVO vo = new StorePrinterVO();
        vo.setStoreId(store.getStoreId());
        vo.setStoreName(store.getStoreName());
        vo.setPlatformReady(feieProperties.configured());
        if (cfg == null) {
            vo.setPrintEnabled(1);
            vo.setVoiceEnabled(1);
            vo.setCopies(1);
            vo.setBound(false);
            return vo;
        }
        vo.setPrinterSn(cfg.getPrinterSn());
        vo.setPrinterKey(cfg.getPrinterKey());
        vo.setPrinterName(cfg.getPrinterName());
        vo.setPrintEnabled(cfg.getPrintEnabled());
        vo.setVoiceEnabled(cfg.getVoiceEnabled());
        vo.setCopies(cfg.getCopies() == null ? 1 : cfg.getCopies());
        vo.setLastStatus(cfg.getLastStatus());
        vo.setLastBindMsg(cfg.getLastBindMsg());
        vo.setBound(StringUtils.hasText(cfg.getPrinterSn()));
        return vo;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
