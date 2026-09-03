package com.example.springboottemplate.utils;

import com.example.springboottemplate.mapper.LogicDeleteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 批量逻辑删除：入参兼容前端 ToStringSerializer 产生的字符串数字 id。
 */
@Component
public class LogicDeleteHelper {

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "user",
            "store",
            "role",
            "menu",
            "dict",
            "area",
            "card",
            "card_buy_log",
            "equ",
            "equ_use_log",
            "contract",
            "contract_template",
            "product",
            "product_catagory",
            "other_business",
            "visitor",
            "mall_order",
            "oper_log"
    );

    @Autowired
    private LogicDeleteMapper logicDeleteMapper;

    public int deleteByIds(String table, List<Long> idList) {
        if (!ALLOWED_TABLES.contains(table)) {
            throw new IllegalArgumentException("不允许删除的表: " + table);
        }
        if (ValidateUtil.isEmpty(idList)) {
            return 0;
        }
        List<Long> ids = idList.stream().filter(id -> id != null).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return 0;
        }
        return logicDeleteMapper.logicDeleteByIds(table, ids);
    }
}
