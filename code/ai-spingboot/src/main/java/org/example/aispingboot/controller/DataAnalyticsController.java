package org.example.aispingboot.controller;

import org.example.aispingboot.common.Result;
import org.example.aispingboot.service.DataAnalyticsService;
import org.example.aispingboot.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台数据统计接口
 */
@RestController
@RequestMapping("/api/data-analytics")
public class DataAnalyticsController {

    @Autowired
    private DataAnalyticsService dataAnalyticsService;

    /**
     * 数据总览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        AuthUtil.requireAdmin();
        return Result.ok(dataAnalyticsService.overview());
    }
}
