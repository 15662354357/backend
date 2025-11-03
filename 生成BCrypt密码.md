# 生成 BCrypt 密码

如果数据库中的默认管理员密码不正确，可以使用以下方法重新生成：

## 方法一：在线工具

访问：https://bcrypt-generator.com/

输入密码：`admin123`
选择轮数：10（默认）
点击生成，复制生成的哈希值

## 方法二：使用 Java 代码

创建一个测试类：

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt密码: " + encodedPassword);
    }
}
```

## 方法三：使用命令行（如果有 Spring Boot CLI）

```bash
spring encodepassword admin123
```

## 更新数据库

```sql
UPDATE sys_user 
SET password = '生成的BCrypt密码' 
WHERE username = 'admin';
```

