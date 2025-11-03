package com.financekb.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传DTO
 */
@Data
public class FileUploadDTO {
    
    /**
     * 上传的文件
     */
    private MultipartFile file;
    
    /**
     * 文件分类
     */
    private String category;
    
    /**
     * 文件标签（逗号分隔）
     */
    private String tags;
    
    /**
     * 文件描述
     */
    private String description;
}

