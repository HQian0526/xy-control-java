package com.example.springboottemplate.service;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Store;

public interface StoreService {
    public Response addStore(Store store);

    public Response findStore(Store store);

    public Response updateStore(Store store);

    public Response deleteStore(int id);
}
