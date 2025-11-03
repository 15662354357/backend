package com.financekb.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件信息视图对象
 */
@Data
public class FileInfoVO {
    
    private Long id;
    private Long userId;
    private String fileName;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 文件大小格式化（KB/MB/GB）
     */
    public String getFormattedSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        
        double size = fileSize;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    /**
     * 是否可预览
     */
    public boolean isPreviewable() {
        if (fileType == null) {
            return false;
        }
        
        String type = fileType.toLowerCase();
        // 图片类型
        if (type.startsWith("image/")) {
            return true;
        }
        // PDF
        if (type.equals("application/pdf")) {
            return true;
        }
        // 视频
        if (type.startsWith("video/")) {
            return true;
        }
        // 音频
        if (type.startsWith("audio/")) {
            return true;
        }
        
        return false;
    }
}

