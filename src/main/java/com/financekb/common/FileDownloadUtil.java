package com.financekb.common;

import org.springframework.http.HttpHeaders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载工具类
 * 用于处理中文文件名的HTTP响应头设置
 * 避免Tomcat编码错误
 */
public class FileDownloadUtil {
    
    /**
     * 设置Content-Disposition响应头（支持中文文件名）
     * 使用RFC 5987标准编码，避免Tomcat编码错误
     * 
     * @param headers HTTP响应头
     * @param dispositionType 类型：attachment（下载）或inline（预览）
     * @param fileName 文件名（可能包含中文）
     */
    public static void setContentDisposition(HttpHeaders headers, String dispositionType, String fileName) {
        try {
            // RFC 5987标准编码（URL编码）
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
            
            // ASCII fallback文件名（必须完全使用ASCII，避免Tomcat编码错误）
            // 如果包含非ASCII字符，使用扩展名+时间戳作为ASCII文件名
            String asciiFileName;
            boolean hasNonAscii = fileName.chars().anyMatch(ch -> ch > 127);
            
            if (hasNonAscii) {
                // 提取文件扩展名
                String fileExt = "";
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot > 0 && lastDot < fileName.length() - 1) {
                    fileExt = fileName.substring(lastDot);
                }
                // 使用纯ASCII文件名（时间戳+扩展名）
                asciiFileName = "file_" + System.currentTimeMillis() + fileExt;
            } else {
                // 如果完全是ASCII，可以使用原文件名（转义引号）
                asciiFileName = fileName.replace("\"", "\\\"");
            }
            
            // 转义ASCII文件名中的引号
            asciiFileName = asciiFileName.replace("\"", "\\\"");
            
            // 构建Content-Disposition头字符串
            // 关键：filename部分只使用ASCII字符，避免Tomcat编码错误
            // filename*=UTF-8''部分使用URL编码，由浏览器解析
            StringBuilder contentDisposition = new StringBuilder(dispositionType);
            contentDisposition.append("; filename=\"");
            contentDisposition.append(asciiFileName);
            contentDisposition.append("\"");
            // 添加RFC 5987编码的文件名（浏览器会优先使用这个）
            contentDisposition.append("; filename*=UTF-8''");
            contentDisposition.append(encodedFileName);
            
            // 使用set方法直接设置完整的header字符串
            // 这样避免Spring和Tomcat尝试编码字符串中的中文部分
            String headerValue = contentDisposition.toString();
            // 移除可能存在的旧值
            headers.remove(HttpHeaders.CONTENT_DISPOSITION);
            // 添加新值（使用add方法，因为我们已经构建了完整的字符串）
            headers.add(HttpHeaders.CONTENT_DISPOSITION, headerValue);
            
        } catch (Exception e) {
            // 如果编码失败，使用纯ASCII文件名
            String asciiFileName = fileName.replaceAll("[^\\x00-\\x7F]", "_");
            if (asciiFileName.equals("_") || asciiFileName.matches("^_+$")) {
                asciiFileName = "file_" + System.currentTimeMillis();
            }
            // 使用Spring的方法（只设置ASCII文件名）
            headers.setContentDispositionFormData(dispositionType, asciiFileName);
        }
    }
}

