package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Store;

import java.util.List;

public interface StoreService {
    Response addStore(Store store);

    Response findStore(Store store);

    Response updateStore(Store store);

    Response deleteStore(List<Integer> idList);
}
