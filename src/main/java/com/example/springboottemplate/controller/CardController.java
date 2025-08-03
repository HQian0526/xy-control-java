package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.Card;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.service.CardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/card")
@Api(tags = "卡管理", description = "卡相关接口")
public class CardController {
    @Autowired
    private CardService cardService;

    //新增卡
    @PostMapping("/addCard")
    @ResponseBody
    @ApiOperation(value = "添加卡", notes = "传入卡片各项信息进行添加卡")
    public Response addUser(@RequestBody Card card, HttpServletRequest request){
        return cardService.addCard(card, request);
    }

    //模糊查询所有卡
    @GetMapping("/findCard")
    @ResponseBody
    @ApiOperation(value = "查询所有卡", notes = "查询卡片表中所有卡")
    public Response findCard(Card card, Integer pageNum, Integer pageSize){
        return cardService.findCard(card, pageNum, pageSize);
    }

    //修改卡信息
    @PutMapping("/updateCard")
    @ResponseBody
    @ApiOperation(value = "修改卡信息", notes = "根据id更新卡信息")
    public Response updateCard(@RequestBody Card card, HttpServletRequest request){
        return cardService.updateCard(card, request);
    }

    //删除卡信息
    @DeleteMapping("/deleteCard")
    @ResponseBody
    @ApiOperation(value = "删除卡", notes = "根据id删除卡")
    public Response deleteCard(@RequestBody List<Integer> idList){
        return cardService.deleteCard(idList);
    }
}
