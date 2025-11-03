package com.financekb.controller;

import com.financekb.common.Result;
import com.financekb.service.FileService;
import com.financekb.vo.FileInfoVO;
import com.financekb.config.FileConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * 文件控制器
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    private final FileConfig fileConfig;
    
    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public Result<FileInfoVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        return fileService.uploadFile(file, userId, category, tags, description);
    }
    
    /**
     * 批量文件上传
     */
    @PostMapping("/upload/batch")
    public Result<?> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        return fileService.uploadFiles(files, userId, category, tags, description);
    }
    
    /**
     * 获取文件列表（分页）
     */
    @GetMapping("/list")
    public Result<?> getFileList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        return fileService.getFileList(userId, keyword, category, tags, current, size);
    }
    
    /**
     * 获取文件详情
     */
    @GetMapping("/{fileId}")
    public Result<FileInfoVO> getFileInfo(@PathVariable Long fileId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        return fileService.getFileInfo(fileId, userId);
    }
    
    /**
     * 文件下载
     * 使用底层方式设置响应头，避免Tomcat编码错误
     */
    @GetMapping("/{fileId}/download")
    public void downloadFile(
            @PathVariable Long fileId, 
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        Result<FileInfoVO> result = fileService.getFileInfo(fileId, userId);
        if (result.getCode() != 200 || result.getData() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        FileInfoVO fileInfo = result.getData();
        // 根据URL反推文件路径
        String urlPath = fileInfo.getFileUrl().replace("/api/files/public/", "");
        // 从配置获取上传路径
        String uploadPath = fileConfig.getUploadPath();
        Path filePath = Paths.get(uploadPath, urlPath);
        
        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // 增加下载次数
        fileService.incrementDownloadCount(fileId);
        
        // 设置Content-Type
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        
        // 设置Content-Disposition头（完全避免中文字符，只使用ASCII）
        String fileName = fileInfo.getFileName();
        
        // 提取文件扩展名（必须是ASCII安全的）
        String fileExt = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            String ext = fileName.substring(lastDot);
            // 确保扩展名只包含ASCII字符
            fileExt = ext.chars()
                .filter(ch -> ch <= 127)
                .mapToObj(ch -> String.valueOf((char)ch))
                .collect(Collectors.joining());
            if (fileExt.isEmpty()) {
                fileExt = ".bin"; // 如果扩展名包含非ASCII，使用默认扩展名
            }
        }
        
        // 生成纯ASCII文件名（时间戳+扩展名）- 作为fallback
        String asciiFileName = "file_" + System.currentTimeMillis() + fileExt;
        
        // 设置响应头（必须在写入响应体之前）
        // 确保响应头在写入响应体之前已经设置完成
        response.setStatus(HttpServletResponse.SC_OK);
        
        try {
            // RFC 5987标准编码（URL编码）
            String encodedFileName = java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
            
            // 只使用 filename*=UTF-8''，避免 filename 部分导致 Tomcat 编码问题
            // 现代浏览器都支持 RFC 5987 标准，会优先使用 filename*
            String headerValue = "attachment; filename*=UTF-8''" + encodedFileName;
            
            log.debug("原始文件名: {}, URL编码文件名: {}, Content-Disposition: {}", 
                fileName, encodedFileName, headerValue);
            
            // 验证 headerValue 完全不包含非ASCII字符（除了URL编码的%XX部分）
            // URL编码的%XX部分是ASCII安全的
            boolean headerHasNonAscii = headerValue.chars()
                .filter(ch -> {
                    // 排除%和十六进制字符
                    return ch != '%' && 
                           !(ch >= '0' && ch <= '9') && 
                           !(ch >= 'A' && ch <= 'F') && 
                           !(ch >= 'a' && ch <= 'f');
                })
                .anyMatch(ch -> ch > 127);
            
            if (headerHasNonAscii) {
                log.error("ERROR: Content-Disposition头仍然包含非ASCII字符！使用fallback");
                // 失败时使用纯ASCII文件名（只设置filename，不设置filename*）
                headerValue = "attachment; filename=\"" + asciiFileName + "\"";
            }
            
            // 移除可能存在的旧响应头
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, null);
            
            // 使用addHeader方法添加响应头（避免Spring Security或其他过滤器覆盖）
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, headerValue);
            
            log.debug("已设置Content-Disposition响应头: {}", headerValue);
            
        } catch (Exception e) {
            log.error("设置文件名失败", e);
            // 失败时使用纯ASCII文件名（只设置filename，不设置filename*）
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, null);
            String fallbackHeader = "attachment; filename=\"" + asciiFileName + "\"";
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, fallbackHeader);
            log.debug("使用fallback响应头: {}", fallbackHeader);
        }
        
        // 确保响应头已经设置完成（在写入响应体之前）
        if (!response.isCommitted()) {
            // 响应头还没有提交，可以安全设置
            log.debug("响应头未提交，可以安全设置");
        } else {
            log.warn("响应头已提交，可能无法设置Content-Disposition");
        }
        
        // 设置文件大小
        response.setContentLengthLong(Files.size(filePath));
        
        // 写入文件内容
        try (var inputStream = Files.newInputStream(filePath);
             var outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        }
    }
    
    /**
     * 文件预览（公开访问）
     */
    @GetMapping("/public/{userId}/{datePath}/{fileName}")
    public ResponseEntity<Resource> previewFile(
            @PathVariable String userId,
            @PathVariable String datePath,
            @PathVariable String fileName) throws IOException {
        
        // 使用配置的上传路径
        String uploadPath = fileConfig.getUploadPath();
        Path filePath = Paths.get(uploadPath, userId, datePath, fileName);
        
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(filePath.toFile());
        String contentType = Files.probeContentType(filePath);
        
        // 如果是图片或PDF，直接预览；否则下载
        boolean isPreviewable = contentType != null && 
            (contentType.startsWith("image/") || contentType.equals("application/pdf") || contentType.startsWith("video/"));
        
        // 预览接口也使用底层方式，避免编码错误
        // 构建Content-Disposition头（完全使用ASCII）
        String fileExt = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            fileExt = fileName.substring(lastDot);
        }
        
        boolean hasNonAscii = fileName.chars().anyMatch(ch -> ch > 127);
        String asciiFileName = hasNonAscii ? "file_" + System.currentTimeMillis() + fileExt : fileName;
        
        String encodedFileName = java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8)
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~");
        
        String dispositionType = isPreviewable ? "inline" : "attachment";
        String contentDisposition = String.format(
            "%s; filename=\"%s\"; filename*=UTF-8''%s",
            dispositionType,
            asciiFileName.replace("\"", "\\\""),
            encodedFileName
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        
        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
            .body(resource);
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public Result<String> deleteFile(@PathVariable Long fileId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        return fileService.deleteFile(fileId, userId);
    }
    
    /**
     * 获取文件分类列表
     */
    @GetMapping("/categories")
    public Result<?> getCategories(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        
        return fileService.getCategories(userId);
    }
}

