package com.financekb.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
}

