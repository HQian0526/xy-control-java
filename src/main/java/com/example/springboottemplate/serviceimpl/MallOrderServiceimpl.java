package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.springboottemplate.config.WxPayProperties;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.mall.MallCheckoutItemRequest;
import com.example.springboottemplate.dto.mall.MallCheckoutRequest;
import com.example.springboottemplate.dto.mall.MallPayPrepareVO;
import com.example.springboottemplate.entity.MallOrder;
import com.example.springboottemplate.entity.MallOrderItem;
import com.example.springboottemplate.entity.Product;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.MallOrderItemMapper;
import com.example.springboottemplate.mapper.MallOrderMapper;
import com.example.springboottemplate.mapper.ProductMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.MallOrderService;
import com.example.springboottemplate.service.wx.WxPayClientService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wechat.pay.java.service.payments.model.Transaction;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class MallOrderServiceimpl implements MallOrderService {

    /** 与小程序约定：2=打烊 */
    private static final int STORE_STATUS_CLOSED = 2;

    @Autowired
    private MallOrderMapper mallOrderMapper;
    @Autowired
    private MallOrderItemMapper mallOrderItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WxPayClientService wxPayClientService;
    @Autowired
    private WxPayProperties wxPayProperties;

    @Override
    public Response checkoutAndPay(MallCheckoutRequest request, HttpServletRequest httpRequest) {
        if (request == null || ValidateUtil.isEmpty(request.getItems())) {
            throw new BusinessException("购物车商品不能为空");
        }
        if (!StringUtils.hasText(request.getContact()) || !request.getContact().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }
        if (!StringUtils.hasText(request.getAddress())) {
            throw new BusinessException("请输入收货地址");
        }

        Claims claims = parseClaims(httpRequest);
        Integer userId = jwtUtil.getUserId(claims);
        String username = claims.getSubject();
        if (userId == null) {
            throw new BusinessException("登录状态无效");
        }

        User user = userMapper.selectById(userId.longValue());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!StringUtils.hasText(user.getOpenid()) && !wxPayClientService.isMock()) {
            throw new BusinessException("当前账号未绑定微信，请先微信登录");
        }

        List<MallOrderItem> itemEntities = new ArrayList<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;
        Long storeId = null;
        Date now = new Date();

        for (MallCheckoutItemRequest line : request.getItems()) {
            if (line == null || line.getProductId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new BusinessException("商品或数量无效");
            }
            Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductId, line.getProductId())
                    .last("limit 1"));
            if (product == null) {
                throw new BusinessException("商品不存在: " + line.getProductId());
            }
            if (product.getProductStatus() == null || product.getProductStatus() != 1) {
                throw new BusinessException("商品已下架: " + product.getProductName());
            }
            if (product.getProductNum() == null || product.getProductNum() < line.getQuantity()) {
                throw new BusinessException("库存不足: " + product.getProductName());
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("商品价格异常: " + product.getProductName());
            }

            Long lineStoreId = parseStoreId(product.getStoreId());
            if (lineStoreId != null) {
                if (storeId == null) {
                    storeId = lineStoreId;
                } else if (!storeId.equals(lineStoreId)) {
                    throw new BusinessException("暂不支持跨店结算，请分店铺下单");
                }
            }

            BigDecimal lineAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(line.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            goodsAmount = goodsAmount.add(lineAmount);

            itemEntities.add(MallOrderItem.builder()
                    .id(IdWorker.getId())
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .productImg(product.getProductImg())
                    .price(product.getPrice())
                    .quantity(line.getQuantity())
                    .amount(lineAmount)
                    .createdTime(now)
                    .deleted(0)
                    .build());
        }

        if (storeId != null) {
            Store store = storeMapper.selectByStoreId(storeId);
            if (store != null && store.getStoreStatus() != null && store.getStoreStatus() == STORE_STATUS_CLOSED) {
                throw new BusinessException("店铺已打烊，请于营业时间下单");
            }
        }

        BigDecimal deliveryFee = wxPayProperties.getDeliveryFee() == null
                ? BigDecimal.ZERO
                : wxPayProperties.getDeliveryFee().setScale(2, RoundingMode.HALF_UP);
        BigDecimal payAmount = goodsAmount.add(deliveryFee).setScale(2, RoundingMode.HALF_UP);
        int payAmountFen = payAmount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        if (payAmountFen <= 0) {
            throw new BusinessException("应付金额必须大于0");
        }

        String orderNo = generateOrderNo();
        Long orderId = IdWorker.getId();
        MallOrder order = MallOrder.builder()
                .id(orderId)
                .orderNo(orderNo)
                .storeId(storeId)
                .userId(userId.longValue())
                .openid(user.getOpenid())
                .contact(request.getContact().trim())
                .address(request.getAddress().trim())
                .remark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null)
                .goodsAmount(goodsAmount)
                .deliveryFee(deliveryFee)
                .payAmount(payAmount)
                .payAmountFen(payAmountFen)
                .payStatus(0)
                .createdBy(username)
                .createdTime(now)
                .deleted(0)
                .build();
        mallOrderMapper.addMallOrder(order);

        for (MallOrderItem item : itemEntities) {
            item.setOrderId(orderId);
            item.setOrderNo(orderNo);
        }
        mallOrderItemMapper.batchAdd(itemEntities);

        String description = itemEntities.size() == 1
                ? itemEntities.get(0).getProductName()
                : "商城订单-" + itemEntities.size() + "件商品";
        MallPayPrepareVO payVO = wxPayClientService.createJsapiPrepay(
                orderNo, description, user.getOpenid(), payAmountFen);

        if (!payVO.isMock() && StringUtils.hasText(payVO.getPackageValue())
                && payVO.getPackageValue().startsWith("prepay_id=")) {
            String prepayId = payVO.getPackageValue().substring("prepay_id=".length());
            mallOrderMapper.updatePrepayId(orderNo, prepayId);
        }

        payVO.setOrderNo(orderNo);
        payVO.setPayAmount(payAmount);
        payVO.setPayStatus(0);
        return Response.success(payVO);
    }

    @Override
    public Response queryOrder(String orderNo, HttpServletRequest httpRequest) {
        MallOrder order = requireOwnedOrder(orderNo, httpRequest);
        // 待支付时尝试向微信查单，补齐回调延迟
        if (order.getPayStatus() != null && order.getPayStatus() == 0 && !wxPayClientService.isMock()) {
            Transaction tx = wxPayClientService.queryByOutTradeNo(orderNo);
            if (tx != null && tx.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                markOrderPaid(orderNo, tx.getTransactionId(), new Date());
                order = mallOrderMapper.selectByOrderNo(orderNo);
            }
        }
        order.setItems(mallOrderItemMapper.selectByOrderId(order.getId()));
        if (order.getStoreId() != null) {
            Store store = storeMapper.selectByStoreId(order.getStoreId());
            order.setStoreName(store != null ? store.getStoreName() : null);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("mock", wxPayClientService.isMock());
        return Response.success(data);
    }

    @Override
    public Response findMallOrder(Integer payStatus, Integer pageNum, Integer pageSize, HttpServletRequest httpRequest) {
        Claims claims = parseClaims(httpRequest);
        Integer userId = jwtUtil.getUserId(claims);
        if (userId == null) {
            throw new BusinessException("登录状态无效");
        }
        User user = userMapper.selectById(userId.longValue());
        Integer identityType = user == null ? null : user.getIdentityType();

        MallOrder query = new MallOrder();
        if (payStatus != null) {
            query.setPayStatus(payStatus);
        }

        if (identityType != null && identityType == 2) {
            // 商户：查本店订单
            Store storeQuery = new Store();
            storeQuery.setUserId(userId.longValue());
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (ValidateUtil.isEmpty(storeList) || storeList.get(0).getStoreId() == null) {
                return buildListResponse(Collections.emptyList(), pageNum, pageSize);
            }
            query.setStoreId(storeList.get(0).getStoreId());
        } else if (identityType != null && identityType == 3) {
            // 管理员：可查全部（仅按 payStatus 过滤）
        } else {
            // 普通用户：仅本人订单
            query.setUserId(userId.longValue());
        }

        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<MallOrder> list = mallOrderMapper.findMallOrder(query);
        list.forEach(item -> {
            item.setItems(mallOrderItemMapper.selectByOrderId(item.getId()));
            if (item.getStoreId() != null) {
                Store store = storeMapper.selectByStoreId(item.getStoreId());
                item.setStoreName(store != null ? store.getStoreName() : null);
            }
        });
        return buildListResponse(list, pageNum, pageSize);
    }

    private Response buildListResponse(List<MallOrder> list, Integer pageNum, Integer pageSize) {
        PageInfo<MallOrder> pageInfo = new PageInfo<>(list);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageNum != null ? pageInfo.getPageNum() : 1);
        data.put("pageSize", pageSize != null ? pageInfo.getPageSize() : pageInfo.getTotal());
        return Response.success(data);
    }

    @Override
    public Response mockConfirmPay(String orderNo, HttpServletRequest httpRequest) {
        if (!wxPayClientService.isMock()) {
            throw new BusinessException("仅 mock 模式可用");
        }
        MallOrder order = requireOwnedOrder(orderNo, httpRequest);
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return Response.success(order);
        }
        if (order.getPayStatus() != null && order.getPayStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        markOrderPaid(orderNo, "MOCK" + orderNo, new Date());
        MallOrder paid = mallOrderMapper.selectByOrderNo(orderNo);
        paid.setItems(mallOrderItemMapper.selectByOrderId(paid.getId()));
        return Response.success(paid);
    }

    @Override
    public String handleWxPayNotify(HttpServletRequest request, String body) {
        try {
            Transaction transaction = wxPayClientService.parseNotify(
                    body,
                    request.getHeader("Wechatpay-Serial"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Signature"),
                    request.getHeader("Wechatpay-Timestamp"),
                    request.getHeader("Wechatpay-Signature-Type")
            );
            if (transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                markOrderPaid(transaction.getOutTradeNo(), transaction.getTransactionId(), new Date());
            }
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            return "{\"code\":\"FAIL\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private void markOrderPaid(String orderNo, String transactionId, Date paidTime) {
        MallOrder order = mallOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return;
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return;
        }
        int rows = mallOrderMapper.markPaid(orderNo, transactionId, paidTime);
        if (rows <= 0) {
            return;
        }
        // 扣库存、加销量（幂等：仅首次 markPaid 成功时执行）
        List<MallOrderItem> items = mallOrderItemMapper.selectByOrderId(order.getId());
        for (MallOrderItem item : items) {
            Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductId, item.getProductId())
                    .last("limit 1"));
            if (product == null) {
                continue;
            }
            int stock = product.getProductNum() == null ? 0 : product.getProductNum();
            int sale = product.getSaleNum() == null ? 0 : product.getSaleNum();
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            product.setProductNum(Math.max(0, stock - qty));
            product.setSaleNum(sale + qty);
            product.setUpdateTime(paidTime);
            productMapper.updateProduct(product);
        }
    }

    private MallOrder requireOwnedOrder(String orderNo, HttpServletRequest httpRequest) {
        if (!StringUtils.hasText(orderNo)) {
            throw new BusinessException("订单号不能为空");
        }
        Claims claims = parseClaims(httpRequest);
        Integer userId = jwtUtil.getUserId(claims);
        MallOrder order = mallOrderMapper.selectByOrderNo(orderNo.trim());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (userId == null) {
            throw new BusinessException("无权查看该订单");
        }
        User user = userMapper.selectById(userId.longValue());
        Integer identityType = user == null ? null : user.getIdentityType();
        // 管理员可查全部
        if (identityType != null && identityType == 3) {
            return order;
        }
        // 下单人可查
        if (order.getUserId() != null && order.getUserId().equals(userId.longValue())) {
            return order;
        }
        // 本店商户可查
        if (identityType != null && identityType == 2 && order.getStoreId() != null) {
            Store storeQuery = new Store();
            storeQuery.setUserId(userId.longValue());
            storeQuery.setDeleted(0);
            List<Store> storeList = storeMapper.findStore(storeQuery);
            if (!ValidateUtil.isEmpty(storeList)
                    && storeList.get(0).getStoreId() != null
                    && storeList.get(0).getStoreId().equals(order.getStoreId())) {
                return order;
            }
        }
        throw new BusinessException("无权查看该订单");
    }

    private Claims parseClaims(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return jwtUtil.parseToken(auth.substring(7));
    }

    private Long parseStoreId(String storeId) {
        if (!StringUtils.hasText(storeId)) {
            return null;
        }
        try {
            return Long.valueOf(storeId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rnd = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "M" + time + rnd;
    }

    private String escapeJson(String msg) {
        if (msg == null) {
            return "error";
        }
        return msg.replace("\"", "'");
    }
}
