package com.financekb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.financekb.config.FileConfig;
import com.financekb.entity.FileInfo;
import com.financekb.mapper.FileInfoMapper;
import com.financekb.common.Result;
import com.financekb.common.ResultCode;
import com.financekb.vo.FileInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final FileInfoMapper fileInfoMapper;
    private final FileConfig fileConfig;
    private final Tika tika = new Tika();
    /**
     * 单文件上传
     */
    @Transactional
    public Result<FileInfoVO> uploadFile(MultipartFile file, Long userId, String category, String tags, String description) {
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !fileConfig.getAllowedTypes().contains(contentType)) {
                return Result.error(ResultCode.FILE_TYPE_NOT_ALLOWED.getCode(), 
                    "不支持的文件类型：" + contentType);
            }
            
            // 验证文件大小
            long fileSize = file.getSize();
            if (fileSize > 100 * 1024 * 1024) { // 100MB
                return Result.error(ResultCode.FILE_SIZE_EXCEEDED.getCode(), 
                    "文件大小不能超过100MB");
            }
            
            // 生成文件存储路径
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "文件名不能为空");
            }
            
            String fileExt = getFileExtension(originalFilename);
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + fileExt;
            
            // 创建存储目录
            Path uploadDir = Paths.get(fileConfig.getUploadPath(), String.valueOf(userId), datePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());
            
            // 检测真实文件类型
            String detectedType = tika.detect(filePath.toFile());
            if (detectedType == null) {
                detectedType = contentType;
            }
            
            // 生成文件URL（相对路径用于预览）
            String fileUrl = "/api/files/public/" + userId + "/" + datePath + "/" + fileName;
            
            // 保存文件信息到数据库
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUserId(userId);
            fileInfo.setFileName(originalFilename);
            fileInfo.setFilePath(filePath.toString());
            fileInfo.setFileUrl(fileUrl);
            fileInfo.setFileType(detectedType);
            fileInfo.setFileSize(fileSize);
            fileInfo.setFileExt(fileExt);
            fileInfo.setCategory(category);
            fileInfo.setTags(tags);
            fileInfo.setDescription(description);
            fileInfo.setDownloadCount(0);
            fileInfo.setViewCount(0);
            
            fileInfoMapper.insert(fileInfo);
            
            log.debug("文件保存路径: {}", filePath.toString());
            log.debug("文件访问URL: {}", fileUrl);
            
            // 转换为VO
            FileInfoVO vo = new FileInfoVO();
            BeanUtils.copyProperties(fileInfo, vo);
            
            log.info("文件上传成功：userId={}, fileName={}, fileId={}", userId, originalFilename, fileInfo.getId());
            return Result.success("文件上传成功", vo);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(ResultCode.FILE_UPLOAD_FAILED.getCode(), "文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量文件上传
     */
    public Result<List<FileInfoVO>> uploadFiles(MultipartFile[] files, Long userId, String category, String tags, String description) {
        if (files == null || files.length == 0) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), "文件不能为空");
        }
        
        if (files.length > 20) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), "一次最多上传20个文件");
        }
        
        List<FileInfoVO> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        
        for (MultipartFile file : files) {
            Result<FileInfoVO> result = uploadFile(file, userId, category, tags, description);
            if (result.getCode() == 200) {
                successList.add(result.getData());
            } else {
                errorList.add(file.getOriginalFilename() + ": " + result.getMessage());
            }
        }
        
        if (successList.isEmpty()) {
            return Result.error("所有文件上传失败：" + String.join("; ", errorList));
        }
        
        String message = "成功上传 " + successList.size() + " 个文件";
        if (!errorList.isEmpty()) {
            message += "，" + errorList.size() + " 个文件失败：" + String.join("; ", errorList);
        }
        
        return Result.success(message, successList);
    }
    
    /**
     * 获取文件列表（分页）
     */
    public Result<IPage<FileInfoVO>> getFileList(Long userId, String keyword, String category, String tags, 
                                                   Integer current, Integer size) {
        Page<FileInfo> page = new Page<>(current != null && current > 0 ? current : 1, 
                                        size != null && size > 0 ? size : 10);
        
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileInfo::getUserId, userId);
        
        // 关键词搜索（文件名、描述）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(FileInfo::getFileName, keyword)
                            .or()
                            .like(FileInfo::getDescription, keyword));
        }
        
        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(FileInfo::getCategory, category);
        }
        
        // 标签筛选
        if (StringUtils.hasText(tags)) {
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                wrapper.like(FileInfo::getTags, tag.trim());
            }
        }
        
        wrapper.orderByDesc(FileInfo::getCreateTime);
        
        IPage<FileInfo> filePage = fileInfoMapper.selectPage(page, wrapper);
        
        // 转换为VO
        IPage<FileInfoVO> voPage = filePage.convert(fileInfo -> {
            FileInfoVO vo = new FileInfoVO();
            BeanUtils.copyProperties(fileInfo, vo);
            return vo;
        });
        
        return Result.success(voPage);
    }
    
    /**
     * 获取文件详情
     */
    public Result<FileInfoVO> getFileInfo(Long fileId, Long userId) {
        FileInfo fileInfo = fileInfoMapper.selectOne(
            new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getId, fileId)
                .eq(FileInfo::getUserId, userId)
        );
        
        if (fileInfo == null) {
            return Result.error(ResultCode.FILE_NOT_FOUND.getCode(), ResultCode.FILE_NOT_FOUND.getMessage());
        }
        
        FileInfoVO vo = new FileInfoVO();
        BeanUtils.copyProperties(fileInfo, vo);
        return Result.success(vo);
    }
    
    /**
     * 删除文件
     */
    @Transactional
    public Result<String> deleteFile(Long fileId, Long userId) {
        FileInfo fileInfo = fileInfoMapper.selectOne(
            new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getId, fileId)
                .eq(FileInfo::getUserId, userId)
        );
        
        if (fileInfo == null) {
            return Result.error(ResultCode.FILE_NOT_FOUND.getCode(), ResultCode.FILE_NOT_FOUND.getMessage());
        }
        
        // 物理删除文件
        try {
            Path filePath = Paths.get(fileInfo.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            log.error("删除物理文件失败：{}", fileInfo.getFilePath(), e);
        }
        
        // 逻辑删除数据库记录
        fileInfoMapper.deleteById(fileId);
        
        log.info("文件删除成功：fileId={}, fileName={}", fileId, fileInfo.getFileName());
        return Result.success("文件删除成功");
    }
    
    /**
     * 获取文件分类列表
     */
    public Result<List<String>> getCategories(Long userId) {
        List<FileInfo> files = fileInfoMapper.selectList(
            new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getUserId, userId)
                .select(FileInfo::getCategory)
                .isNotNull(FileInfo::getCategory)
                .ne(FileInfo::getCategory, "")
        );
        
        Set<String> categories = files.stream()
            .map(FileInfo::getCategory)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        return Result.success(new ArrayList<>(categories));
    }
    
    /**
     * 增加下载次数
     */
    @Transactional
    public void incrementDownloadCount(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo != null) {
            fileInfo.setDownloadCount((fileInfo.getDownloadCount() == null ? 0 : fileInfo.getDownloadCount()) + 1);
            fileInfoMapper.updateById(fileInfo);
        }
    }
    
    /**
     * 增加查看次数
     */
    @Transactional
    public void incrementViewCount(Long fileId) {
        FileInfo fileInfo = fileInfoMapper.selectById(fileId);
        if (fileInfo != null) {
            fileInfo.setViewCount((fileInfo.getViewCount() == null ? 0 : fileInfo.getViewCount()) + 1);
            fileInfoMapper.updateById(fileInfo);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}

