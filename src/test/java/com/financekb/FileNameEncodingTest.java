package com.financekb;

import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件名编码测试
 */
public class FileNameEncodingTest {
    
    @Test
    public void testChineseFileName() {
        String fileName = "招商信诺历史费控系统历史数据迁移方案评审.docx";
        
        // 检查是否包含非ASCII字符
        boolean hasNonAscii = fileName.chars().anyMatch(ch -> ch > 127);
        System.out.println("是否包含非ASCII字符: " + hasNonAscii);
        
        // 生成ASCII文件名
        String fileExt = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            fileExt = fileName.substring(lastDot);
        }
        String asciiFileName = hasNonAscii ? "file_" + System.currentTimeMillis() + fileExt : fileName;
        System.out.println("ASCII文件名: " + asciiFileName);
        
        // URL编码
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~");
        System.out.println("URL编码文件名: " + encodedFileName);
        
        // 构建Content-Disposition头
        String contentDisposition = String.format(
            "attachment; filename=\"%s\"; filename*=UTF-8''%s",
            asciiFileName,
            encodedFileName
        );
        System.out.println("Content-Disposition: " + contentDisposition);
        
        // 验证不包含非ASCII字符（除了%XX部分）
        boolean containsNonAscii = contentDisposition.chars()
            .filter(ch -> ch > 127 && ch != '%')
            .anyMatch(ch -> {
                // 检查是否在%XX模式中
                return true;
            });
        System.out.println("是否包含非ASCII（除URL编码）: " + containsNonAscii);
    }
}

