package com.financekb;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成 BCrypt 加密密码
 */
public class PasswordGeneratorTest {
    
    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成默认管理员密码
        String adminPassword = encoder.encode("admin123");
        System.out.println("管理员密码（admin123）: " + adminPassword);
        
        // 验证密码
        boolean matches = encoder.matches("admin123", adminPassword);
        System.out.println("密码验证结果: " + matches);
    }
}

