@echo off
chcp 65001 >nul
echo ========================================
echo 登录功能测试脚本
echo ========================================
echo.

echo [1] 测试登录接口...
echo.
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
echo.
echo.

echo ========================================
echo 测试完成
echo ========================================
echo.
echo 说明：
echo - 如果看到 "code": 200，说明登录成功
echo - "data" 字段中的值是 JWT Token，请保存用于后续请求
echo.
echo 测试获取用户信息（需要先登录获取Token）：
echo curl -X GET http://localhost:8080/api/user/info -H "Authorization: Bearer YOUR_TOKEN"
echo.
pause

