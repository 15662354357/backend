package com.financekb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件信息实体
 */
@Data
@TableName("file_info")
public class FileInfo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String fileName;
    
    private String filePath;
    
    private String fileUrl;
    
    private String fileType;
    
    private Long fileSize;
    
    private String fileExt;
    
    private String category;
    
    private String tags;
    
    private String description;
    
    private String shareCode;
    
    private LocalDateTime shareExpireTime;
    
    private Integer downloadCount;
    
    private Integer viewCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}

