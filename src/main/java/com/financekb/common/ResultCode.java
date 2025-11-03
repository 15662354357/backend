package com.financekb.common;

/**
 * 响应状态码
 */
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),
    
    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    USERNAME_EXISTS(1004, "用户名已存在"),
    EMAIL_EXISTS(1005, "邮箱已被注册"),
    PHONE_EXISTS(1006, "手机号已被注册"),
    LOGIN_FAILED(1007, "登录失败"),
    
    // 文件相关
    FILE_NOT_FOUND(2001, "文件不存在"),
    FILE_UPLOAD_FAILED(2002, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(2003, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(2004, "文件大小超限"),
    
    // AI相关
    AI_API_ERROR(3001, "AI API调用失败"),
    AI_SERVICE_UNAVAILABLE(3002, "AI服务不可用"),
    AI_QUOTA_EXCEEDED(3003, "AI配额已用完"),
    
    // 知识库相关
    KNOWLEDGE_BASE_NOT_FOUND(4001, "知识库不存在"),
    DOCUMENT_NOT_FOUND(4002, "文档不存在");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

