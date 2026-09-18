package com.example.springboottemplate.mapper;

import com.example.springboottemplate.entity.StorePrinter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StorePrinterMapper {
    void addStorePrinter(StorePrinter record);

    void updateStorePrinter(StorePrinter record);

    StorePrinter selectByStoreId(@Param("storeId") Long storeId);

    StorePrinter selectAnyBySn(@Param("printerSn") String printerSn);

    List<StorePrinter> findStorePrinter(StorePrinter query);
}
