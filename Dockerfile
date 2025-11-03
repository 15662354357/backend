# 多阶段构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制jar文件
COPY --from=builder /app/target/*.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/uploads /app/logs

# 安装必要的工具
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "app.jar"]

