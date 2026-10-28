package com.example.springboottemplate.service;

import com.example.springboottemplate.dto.Response;
import jakarta.servlet.http.HttpServletRequest;

public interface DashboardService {
    Response overview(HttpServletRequest request);
}
