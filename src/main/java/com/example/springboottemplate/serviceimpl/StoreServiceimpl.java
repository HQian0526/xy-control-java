package com.example.springboottemplate.serviceimpl;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.service.StoreService;
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
        this.storeMapper.addStore(store);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findStore() {
        List<Store> list = (List<Store>) this.storeMapper.findStore();
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response selectStoreById(int id) {
        List<Store> list = (List<Store>) this.storeMapper.selectStoreById(id);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateStore(Store store) {
        this.storeMapper.updateStore(store);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteStore(int id) {
        this.storeMapper.deleteStore(id);
        return new Response(200, null, "操作成功");
    }
}
