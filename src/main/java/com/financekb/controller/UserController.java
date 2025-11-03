package com.financekb.controller;

import com.financekb.common.Result;
import com.financekb.service.UserService;
import com.financekb.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("X-User-Id");
        if (userId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Long) {
                userId = (Long) authentication.getPrincipal();
            }
        }
        if (userId == null) {
            return Result.error(401, "未授权");
        }
        return userService.getUserInfo(userId);
    }
}

