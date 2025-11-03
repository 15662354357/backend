package com.financekb.controller;

import com.financekb.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    
    @GetMapping
    public Result<String> health() {
        return Result.success("服务运行正常");
    }
}

