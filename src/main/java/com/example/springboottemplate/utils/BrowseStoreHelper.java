package com.example.springboottemplate.utils;

import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.mapper.StoreMapper;

import java.util.List;

/**
 * 逛店查询用的店铺：商家扫别人店的码时按请求 storeId 看货，未传则仍看自己的店。
 */
public final class BrowseStoreHelper {

    private BrowseStoreHelper() {
    }

    public static String findOwnStoreId(Long userId, StoreMapper storeMapper) {
        if (userId == null || storeMapper == null) {
            return null;
        }
        Store storeQuery = new Store();
        storeQuery.setUserId(userId);
        storeQuery.setDeleted(0);
        List<Store> storeList = storeMapper.findStore(storeQuery);
        if (ValidateUtil.isEmpty(storeList) || storeList.get(0).getStoreId() == null) {
            return null;
        }
        return String.valueOf(storeList.get(0).getStoreId());
    }

    /**
     * @return 应查询的 storeId；商家无绑定店铺时返回 null
     */
    public static String resolveMerchantBrowseStoreId(Long userId, String requestedStoreId, StoreMapper storeMapper) {
        String own = findOwnStoreId(userId, storeMapper);
        if (own == null) {
            return null;
        }
        if (requestedStoreId == null || requestedStoreId.trim().isEmpty() || requestedStoreId.equals(own)) {
            return own;
        }
        return requestedStoreId.trim();
    }
}
