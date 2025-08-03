package com.example.springboottemplate.service.contract;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboottemplate.dto.Response;
import com.example.springboottemplate.dto.contract.ContractDTO;
import com.example.springboottemplate.dto.contract.ContractDetailVO;
import com.example.springboottemplate.dto.contract.ContractQueryDTO;
import com.example.springboottemplate.entity.contract.Contract;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ContractService extends IService<Contract> {
    Response addContract(Contract contract, HttpServletRequest request);

    Response findContract(Contract contract, Integer pageNum, Integer pageSize);

    Response updateContract(Contract contract, HttpServletRequest request);

    Response deleteContract(List<Integer> idList);

    Page<Contract> queryContractPage(ContractQueryDTO queryDTO);
    ContractDetailVO getContractDetail(Long id);
    boolean saveContract(ContractDTO contractDTO);
    boolean terminateContract(Long id, String reason);
}
