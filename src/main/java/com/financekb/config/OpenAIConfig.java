package com.financekb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * API基础URL
     */
    private String baseUrl;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 最大Token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * 超时时间（毫秒）
     */
    private Integer timeout;
}

