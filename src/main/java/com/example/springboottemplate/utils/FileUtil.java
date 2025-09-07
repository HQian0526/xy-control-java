package com.example.springboottemplate.utils;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileUtil {
    static {
        // 初始化中文字体映射
        Map<String, String> fontMap = new HashMap<>();
        fontMap.put("宋体", "SimSun");
        fontMap.put("新宋体", "NSimSun");
        fontMap.put("黑体", "SimHei");
        fontMap.put("楷体", "KaiTi");
        fontMap.put("仿宋", "FangSong");
        fontMap.put("微软雅黑", "Microsoft YaHei");

        // 设置字体映射
        System.setProperty("docx4j.font.mapper.classname", "org.docx4j.fontmapper.SimpleFontMapper");
        for (Map.Entry<String, String> entry : fontMap.entrySet()) {
            System.setProperty("docx4j.font.mapper.classname." + entry.getKey(), entry.getValue());
        }
    }

    public byte[] convertToPdf(InputStream wordStream) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(wordStream);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

        // 使用toPDF方法
        Docx4J.toPDF(wordMLPackage, pdfOutputStream);

        return pdfOutputStream.toByteArray();
    }
}
