package com.financekb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 数据库初始化辅助工具
 * 用于生成 BCrypt 密码和其他初始化数据
 */
@SpringBootTest
public class DatabaseInitHelper {
    
    /**
     * 生成 admin123 的 BCrypt 密码
     * 用于更新数据库中的默认管理员密码
     */
    @Test
    public void generateAdminPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("生成 BCrypt 密码");
        System.out.println("========================================");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt密码: " + encodedPassword);
        System.out.println();
        System.out.println("更新 SQL 语句：");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
        System.out.println("========================================");
        
        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("密码验证: " + (matches ? "✓ 正确" : "✗ 错误"));
    }
}

