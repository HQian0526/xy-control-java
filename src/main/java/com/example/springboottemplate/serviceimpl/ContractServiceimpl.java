package com.example.springboottemplate.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.contract.*;
import com.example.springboottemplate.entity.contract.Contract;
import com.example.springboottemplate.entity.Store;
import com.example.springboottemplate.entity.contract.ContractItem;
import com.example.springboottemplate.entity.contract.PayRecord;
import com.example.springboottemplate.entity.system.User;
import com.example.springboottemplate.enums.ContractStatus;
import com.example.springboottemplate.exception.BusinessException;
import com.example.springboottemplate.mapper.contract.ContractItemMapper;
import com.example.springboottemplate.mapper.contract.ContractMapper;
import com.example.springboottemplate.mapper.StoreMapper;
import com.example.springboottemplate.mapper.contract.PayRecordMapper;
import com.example.springboottemplate.mapper.system.UserMapper;
import com.example.springboottemplate.service.contract.ContractService;
import com.example.springboottemplate.utils.JwtUtil;
import com.example.springboottemplate.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractServiceimpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private JwtUtil jwtUtil;  // 注入 JwtUtil
    @Autowired
    private ContractItemMapper contractItemMapper;
    @Autowired
    private PayRecordMapper payRecordMapper;

    @Override
    public Response addContract(Contract contract, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        contract.setCreatedTime(new Date());
        contract.setCreatedBy(username);
        contract.setContractNo(generateContractNo());
        // 仅当 contractStatus 为 null 时才设置为 1（待签署）
        if (contract.getContractStatus() == null) {
            contract.setContractStatus(1);
        }
        contractMapper.addContract(contract);
        return new Response(200, contract, "操作成功");
    }

    @Override
    public Response findContract(Contract contract, Integer pageNum, Integer pageSize) {
        // 开启分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询数据
        List<Contract> list = contractMapper.findContract(contract);
        // 封装分页结果
        PageInfo<Contract> pageInfo = new PageInfo<>(list);
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
    public Response updateContract(Contract contract, HttpServletRequest request) {
        // 1. 从请求头中获取JWT令牌
        String token = request.getHeader("Authorization").substring(7);
        // 2. 解析令牌获取用户名
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.getSubject();
        contract.setUpdateBy(username);
        contractMapper.updateContract(contract);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteContract(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        Integer affectedRows = contractMapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }

    @Override
    public Page<Contract> queryContractPage(ContractQueryDTO queryDTO) {
        return lambdaQuery()
                .eq(queryDTO.getContractType() != null, Contract::getContractType, queryDTO.getContractType())
                .eq(queryDTO.getContractStatus() != null, Contract::getContractStatus, queryDTO.getContractStatus())
                .like(StringUtils.isNotBlank(queryDTO.getContractName()), Contract::getContractName, queryDTO.getContractName())
                .like(StringUtils.isNotBlank(queryDTO.getContractNo()), Contract::getContractNo, queryDTO.getContractNo())
                .page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Override
    public ContractDetailVO getContractDetail(String no) {
        Contract contract = getById(no);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        ContractDetailVO detailVO = new ContractDetailVO();
        BeanUtils.copyProperties(contract, detailVO);

        // 查询合同明细
        List<ContractItem> items = contractItemMapper.selectList(
                new LambdaQueryWrapper<ContractItem>()
                        .eq(ContractItem::getContractNo, no)
        );
        // 转换为VO列表
        List<ContractItemVO> itemVOs = items.stream()
                .map(item -> {
                    ContractItemVO vo = new ContractItemVO();
                    BeanUtils.copyProperties(item, vo); // 复制属性
                    return vo;
                })
                .collect(Collectors.toList());
        detailVO.setItems(itemVOs);

        // 查询付款记录
        List<PayRecord> payRecords = payRecordMapper.selectList(
                new LambdaQueryWrapper<PayRecord>()
                        .eq(PayRecord::getContractNo, no)
                        .orderByDesc(PayRecord::getPaymentDate)
        );
        List<PayRecordVO> paymentRecordVOs = payRecords.stream()
                .map(record -> {
                    PayRecordVO vo = new PayRecordVO();
                    BeanUtils.copyProperties(record, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        detailVO.setPaymentRecords(paymentRecordVOs);

        return detailVO;
    }

    @Override
    @Transactional
    public boolean saveContract(ContractDTO contractDTO, HttpServletRequest request) {
        Contract contract = new Contract();
        BeanUtils.copyProperties(contractDTO, contract);
        // 保存合同基本信息
        if (contract.getContractNo() == null) {
            // 1. 从请求头中获取JWT令牌
            String token = request.getHeader("Authorization").substring(7);
            // 2. 解析令牌获取用户名
            Claims claims = jwtUtil.parseToken(token);
            String username = claims.getSubject();
            contract.setCreatedTime(new Date());
            contract.setCreatedBy(username);
            contract.setContractNo(generateContractNo());
            contract.setContractStatus(1); // 待签署
            save(contract);
        } else {
//            updateById(contract);
        }

        // 先删除原有明细
        contractItemMapper.delete(
                new LambdaQueryWrapper<ContractItem>()
                        .eq(ContractItem::getContractNo, contract.getContractNo())
        );

        // 保存合同明细
        if (CollectionUtils.isNotEmpty(contractDTO.getItems())) {
            List<ContractItem> items = contractDTO.getItems().stream()
                    .map(itemDTO -> {
                        ContractItem item = new ContractItem();
                        BeanUtils.copyProperties(itemDTO, item);
                        item.setContractNo(contract.getContractNo());
                        return item;
                    }).collect(Collectors.toList());

            contractItemMapper.insertBatchSomeColumn(items);
        }

        return true;
    }

    private String generateContractNo() {
        // 生成合同编号逻辑，如 HT20230801001
        return "HT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                String.format("%03d", new Random().nextInt(1000));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean terminateContract(Long contractId, String reason) {
        // 1. 校验合同是否存在且状态允许终止
        Contract contract = getById(contractId);
        if (contract == null) {
            throw new BusinessException("合同不存在");
        }

        // 2. 校验合同状态（只有已生效或待签署的合同可终止）
        if (contract.getContractStatus() != ContractStatus.EFFECTIVE.getCode()
                && contract.getContractStatus() != ContractStatus.PENDING_SIGN.getCode()) {
            throw new BusinessException("当前合同状态不允许终止");
        }

        // 3. 更新合同状态为"已终止"
        contract.setContractStatus(ContractStatus.TERMINATED.getCode());
        contract.setRemark(reason);
        updateById(contract);

        return true;
    }
}
