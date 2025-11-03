package com.financekb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 财务会计知识库系统启动类
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("com.financekb.mapper")
public class FinanceKnowledgeBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceKnowledgeBaseApplication.class, args);
    }
}

