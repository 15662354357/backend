package com.financekb.service;

import com.financekb.dto.LoginDTO;
import com.financekb.dto.RegisterDTO;
import com.financekb.entity.SysUser;
import com.financekb.mapper.SysUserMapper;
import com.financekb.common.JwtUtil;
import com.financekb.common.Result;
import com.financekb.common.ResultCode;
import com.financekb.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户注册
     */
    public Result<String> register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        SysUser existUser = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, registerDTO.getUsername())
                .last("LIMIT 1")
        );
        if (existUser != null) {
            return Result.error(ResultCode.USERNAME_EXISTS.getCode(), ResultCode.USERNAME_EXISTS.getMessage());
        }
        
        // 检查邮箱是否存在
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            existUser = userMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getEmail, registerDTO.getEmail())
                    .last("LIMIT 1")
            );
            if (existUser != null) {
                return Result.error(ResultCode.EMAIL_EXISTS.getCode(), ResultCode.EMAIL_EXISTS.getMessage());
            }
        }
        
        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setStatus(1);
        
        userMapper.insert(user);
        
        log.info("用户注册成功：{}", registerDTO.getUsername());
        return Result.success("注册成功");
    }
    
    /**
     * 用户登录
     */
    public Result<String> login(LoginDTO loginDTO, String ip) {
        SysUser user = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, loginDTO.getUsername())
                .last("LIMIT 1")
        );
        
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        if (user.getStatus() == 0) {
            return Result.error(ResultCode.USER_DISABLED.getCode(), ResultCode.USER_DISABLED.getMessage());
        }
        
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR.getCode(), ResultCode.PASSWORD_ERROR.getMessage());
        }
        
        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        log.info("用户登录成功：{}", loginDTO.getUsername());
        return Result.success("登录成功", token);
    }
    
    /**
     * 获取用户信息
     */
    public Result<UserVO> getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(), ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return Result.success(userVO);
    }
}

