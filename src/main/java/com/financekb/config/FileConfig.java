package com.financekb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 文件配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileConfig {
    
    /**
     * 文件上传路径
     */
    private String uploadPath;
    
    /**
     * 文件访问URL前缀
     */
    private String accessUrl;
    
    /**
     * 允许的文件类型
     */
    private List<String> allowedTypes;
}

