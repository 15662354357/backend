-- 财务会计知识库系统数据库脚本

CREATE DATABASE IF NOT EXISTS `finance_kb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `finance_kb`;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(500) COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
  `type` TINYINT COMMENT '类型：1-菜单，2-按钮',
  `path` VARCHAR(200) COMMENT '路由路径',
  `icon` VARCHAR(50) COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 文件信息表
CREATE TABLE IF NOT EXISTS `file_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `user_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_url` VARCHAR(500) COMMENT '文件访问URL',
  `file_type` VARCHAR(50) COMMENT '文件类型（MIME）',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `file_ext` VARCHAR(20) COMMENT '文件扩展名',
  `category` VARCHAR(50) COMMENT '文件分类',
  `tags` VARCHAR(255) COMMENT '标签（逗号分隔）',
  `description` TEXT COMMENT '文件描述',
  `share_code` VARCHAR(50) COMMENT '分享码',
  `share_expire_time` DATETIME COMMENT '分享过期时间',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `view_count` INT DEFAULT 0 COMMENT '查看次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_share_code` (`share_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
  `description` TEXT COMMENT '知识库描述',
  `cover_image` VARCHAR(500) COMMENT '封面图片',
  `category_id` BIGINT COMMENT '分类ID',
  `is_public` TINYINT DEFAULT 0 COMMENT '是否公开：0-私有，1-公开',
  `document_count` INT DEFAULT 0 COMMENT '文档数量',
  `view_count` INT DEFAULT 0 COMMENT '查看次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 知识库分类表
CREATE TABLE IF NOT EXISTS `knowledge_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类表';

-- 知识库文档表
CREATE TABLE IF NOT EXISTS `knowledge_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
  `file_id` BIGINT COMMENT '关联文件ID',
  `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
  `content` LONGTEXT COMMENT '文档内容',
  `content_summary` TEXT COMMENT '内容摘要',
  `vector_id` VARCHAR(100) COMMENT '向量数据库ID',
  `version` INT DEFAULT 1 COMMENT '版本号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_knowledge_base_id` (`knowledge_base_id`),
  KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 对话记录表
CREATE TABLE IF NOT EXISTS `chat_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `session_id` VARCHAR(100) COMMENT '会话ID',
  `message_type` TINYINT COMMENT '消息类型：1-用户，2-助手',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `knowledge_base_id` BIGINT COMMENT '关联知识库ID',
  `token_usage` INT COMMENT 'Token使用量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话记录表';

-- AI生成记录表
CREATE TABLE IF NOT EXISTS `ai_generation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `generation_type` VARCHAR(50) COMMENT '生成类型：text/voice/image/video/document',
  `prompt` TEXT COMMENT '提示词',
  `result` TEXT COMMENT '生成结果（JSON格式）',
  `token_usage` INT COMMENT 'Token使用量',
  `cost` DECIMAL(10, 4) COMMENT '成本（美元）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_generation_type` (`generation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成记录表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `description` VARCHAR(255) COMMENT '配置描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT COMMENT '用户ID',
  `operation` VARCHAR(100) COMMENT '操作类型',
  `method` VARCHAR(200) COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `location` VARCHAR(100) COMMENT 'IP归属地',
  `status` TINYINT COMMENT '状态：0-失败，1-成功',
  `error_msg` TEXT COMMENT '错误信息',
  `execute_time` BIGINT COMMENT '执行时间（毫秒）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 插入初始角色数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'admin', '系统管理员，拥有所有权限'),
('普通用户', 'user', '普通用户，基础权限'),
('财务专家', 'expert', '财务专家，高级权限');

-- 插入初始管理员用户（密码：admin123，需要加密）
-- 默认密码：admin123，BCrypt加密后的值需要程序生成
INSERT INTO `sys_user` (`username`, `password`, `email`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyZHl5X6hKVNXqZ8LlOZqs5pD4K', 'admin@example.com', '管理员', 1);

-- 分配管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

