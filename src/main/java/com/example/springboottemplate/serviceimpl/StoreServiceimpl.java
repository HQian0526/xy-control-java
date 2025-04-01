package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.service.StoreService;
import com.example.springboottemplate.utils.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StoreServiceimpl implements StoreService {
    @Autowired
    private StoreMapper storeMapper;
    @Override
    public Response addStore(Store store) {
        storeMapper.addStore(store);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findStore(Store store) {
        List<Store> list = storeMapper.findStore(store);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateStore(Store store) {
        storeMapper.updateStore(store);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteStore(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = storeMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
