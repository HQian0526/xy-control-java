package com.example.springboottemplate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用逻辑删除（绕过 MP TableLogic 对 deleted=0 的硬过滤，兼容 deleted 为 null）
 */
@Mapper
public interface LogicDeleteMapper {

    @UpdateProvider(type = LogicDeleteSqlProvider.class, method = "logicDeleteByIds")
    int logicDeleteByIds(@Param("table") String table, @Param("idList") List<Long> idList);

    class LogicDeleteSqlProvider {
        public String logicDeleteByIds(Map<String, Object> params) {
            @SuppressWarnings("unchecked")
            List<Long> idList = (List<Long>) params.get("idList");
            String table = String.valueOf(params.get("table"));
            String ids = idList.stream().map(String::valueOf).collect(Collectors.joining(","));
            return "UPDATE `" + table + "` SET deleted = 1 WHERE id IN (" + ids
                    + ") AND (deleted = 0 OR deleted IS NULL)";
        }
    }
}
