package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StoreController {
    @Autowired
    private StoreService storeService;

    //添加商户
    @RequestMapping("/addStore")
    @ResponseBody
    public Response addStore(Store store){
        return this.storeService.addStore(store);
    }

    //查询所有商户
    @RequestMapping("/findStore")
    @ResponseBody
    public Response findStore(){
        return this.storeService.findStore();
    }

    //根据id查询商户信息
    @RequestMapping("/selectStoreById")
    @ResponseBody
    public Response selectStoreById(int id){
        return this.storeService.selectStoreById(id);
    }

    //修改用户信息
    @RequestMapping("/updateStore")
    @ResponseBody
    public Response updateStore(Store store){
        return this.storeService.updateStore(store);
    }

    //删除用户信息（慎用）
    @RequestMapping("/deleteStore")
    @ResponseBody
    public Response deleteStore(int id){
        return this.storeService.deleteStore(id);
    }
}
