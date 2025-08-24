package com.example.springboottemplate.serviceimpl.contract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.contract.ContractDetailVO;
import com.example.springboottemplate.dto.contract.ContractItemVO;
import com.example.springboottemplate.entity.contract.Contract;
import com.example.springboottemplate.entity.contract.ContractTemplate;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.contract.TemplateMapper;
import com.example.springboottemplate.service.contract.TemplateService;
import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TemplateServiceimpl extends ServiceImpl<TemplateMapper, ContractTemplate> implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil
    @Value("${upload.dir}")
    private String templateUploadPath;

    @Override
    public Response addContractTemp(ContractTemplate contractTemplate, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        contractTemplate.setCreatedTime(new Date());
        contractTemplate.setCreatedBy(username);
        contractTemplate.setTemplateNo(generateNo());
        contractTemplate.setDeleted(0);
        return Response.success(templateMapper.insert(contractTemplate));
    }

    @Override
    public Response findContractTemp(ContractTemplate contractTemplate, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<ContractTemplate> list = templateMapper.findContractTemp(contractTemplate);
        // 封装分页结果
        PageInfo<ContractTemplate> pageInfo = new PageInfo<>(list);
        // 构造返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageInfo.getList());  // 当前页数据
        data.put("total", pageInfo.getTotal()); // 总记录数
        data.put("pages", pageInfo.getPages()); // 总页数
        data.put("pageNum", pageInfo.getPageNum()); // 当前页码
        data.put("pageSize", pageInfo.getPageSize()); // 每页数量
        return new Response(200, data, "操作成功");
    }

    @Override
    public Response updateContractTemp(ContractTemplate contractTemplate, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        contractTemplate.setUpdateBy(username);
        templateMapper.updateById(contractTemplate);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteContractTemp(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        Integer affectedRows = templateMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }

    @Override
    public ContractTemplate getTemplateByContractType(Integer contractType) {
        return templateMapper.selectOne(
                new LambdaQueryWrapper<ContractTemplate>()
                        .eq(ContractTemplate::getTemplateType, contractType)
                        .eq(ContractTemplate::getDeleted, 0) // 只查询启用的模板
                        .last("LIMIT 1") // 确保只返回一条记录
        );
    }

    @Override
    public byte[] fillTemplate(String templateNo, ContractDetailVO contractDetailVO) throws Exception {
        QueryWrapper<ContractTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("template_no", templateNo);
        ContractTemplate template = templateMapper.selectOne(wrapper);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 1. 加载模板文件
        File templateFile = new File(templateUploadPath + template.getFilePath());
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateFile);

        // 2. 准备数据
        Map<String, String> data = prepareTemplateData(contractDetailVO);

        // 3. 使用docx4j填充变量
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
        VariablePrepare.prepare(wordMLPackage);
        documentPart.variableReplace(data);

        // 4. 生成文档字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wordMLPackage.save(out);
        return out.toByteArray();
    }

    private Map<String, String> prepareTemplateData(ContractDetailVO contractDetailVO) {
        Map<String, String> data = new HashMap<>();

        // 基础合同信息
        data.put("contractNo", contractDetailVO.getContractNo());
        data.put("contractName", contractDetailVO.getContractName());
        data.put("lessorName", contractDetailVO.getLessorName());
        data.put("lesseeName", contractDetailVO.getLesseeName());

        // 日期格式化
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        data.put("startDate", contractDetailVO.getStartDate().format(dateFormatter));
        data.put("endDate", contractDetailVO.getEndDate().format(dateFormatter));

        // 金额格式化
        data.put("totalAmount", contractDetailVO.getTotalAmount().setScale(2).toString());

        // 合同项列表
        if (CollectionUtils.isNotEmpty(contractDetailVO.getItems())) {
            for (int i = 0; i < contractDetailVO.getItems().size(); i++) {
                ContractItemVO item = contractDetailVO.getItems().get(i);
                data.put("itemName_" + i, item.getItemName());
                data.put("quantity_" + i, String.valueOf(item.getQuantity()));
                // 其他字段...
            }
        }
        return data;
    }

    @Override
    public Map<String, Object> previewTemplateVariables(String templateNo) {
        QueryWrapper<ContractTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("template_no", templateNo);
        ContractTemplate template = templateMapper.selectOne(wrapper);
        if (template == null) throw new BusinessException("模板不存在");

        // 假设 variablesConfig 是 JSON 字段，存储变量定义
        // 格式示例：{"variables":[{"name":"contractNo","source":"contract"}]}
        return JSON.parseObject(template.getVariablesConfig(), Map.class);
    }

    @Override
    public ResponseEntity<?> previewTemplate(String templateNo) {
        // 1. 查询模板信息
        QueryWrapper<ContractTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("template_no", templateNo);
        ContractTemplate template = templateMapper.selectOne(wrapper);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        // 2. 构建文件路径
        String filePath = templateUploadPath + template.getFilePath();
        Path path = Paths.get(filePath);

        try {
            // 3. 检查文件是否存在
            if (!Files.exists(path)) {
                throw new BusinessException("模板文件不存在");
            }

            // 4. 创建Resource对象
            Resource resource = new UrlResource(path.toUri());

            // 处理文件名编码
            String encodedFileName = URLEncoder.encode(template.getFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            // 5. 确定内容类型
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 6. 构建响应
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(
                    ContentDisposition.builder("inline")
                            .filename(encodedFileName, StandardCharsets.UTF_8)
                            .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            throw new BusinessException("预览模板失败: " + e.getMessage());
        }
    }

    private String generateNo() {
        // 生成合同编号逻辑，如 HT20230801001
        return "MB" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                String.format("%03d", new Random().nextInt(1000));
    }
}
