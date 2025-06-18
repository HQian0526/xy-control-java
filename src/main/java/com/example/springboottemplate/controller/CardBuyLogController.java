package com.example.springboottemplate.controller;

import com.example.springboottemplate.entity.CardBuyLog;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.service.CardBuyLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cardBuyLog")
@Api(tags = "套餐购买记录管理", description = "套餐购买记录相关接口")
public class CardBuyLogController {
    @Autowired
    private CardBuyLogService cardBuyLogService;

    // 新增卡片购买记录
    @PostMapping("/addCardBuyLog")
    @ResponseBody
    @ApiOperation(value = "添加套餐购买记录", notes = "进行添加套餐购买记录")
    public Response addCardBuyLog(CardBuyLog cardBuyLog){
        return cardBuyLogService.addCardBuyLog(cardBuyLog);
    }

    //查询所有套餐购买记录
    @GetMapping("/findCardBuyLog")
    @ResponseBody
    @ApiOperation(value = "查询所有套餐购买记录", notes = "查询套餐购买记录表中所有套餐购买记录")
    public Response findCardBuyLog(CardBuyLog cardBuyLog, Integer pageNum, Integer pageSize){
        return cardBuyLogService.findCardBuyLog(cardBuyLog, pageNum, pageSize);
    }

    //修改套餐购买记录信息
    @PutMapping("/updateCardBuyLog")
    @ResponseBody
    @ApiOperation(value = "修改套餐购买记录信息", notes = "根据id更新套餐购买记录信息")
    public Response updateCardBuyLog(CardBuyLog cardBuyLog){
        return cardBuyLogService.updateCardBuyLog(cardBuyLog);
    }

    //删除套餐购买记录信息
    @DeleteMapping("/deleteCardBuyLog")
    @ResponseBody
    @ApiOperation(value = "删除套餐购买记录", notes = "根据id删除套餐购买记录")
    public Response deleteCardBuyLog(List<Integer> idList){
        return cardBuyLogService.deleteCardBuyLog(idList);
    }
}
