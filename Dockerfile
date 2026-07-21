# 镜像
FROM openjdk:21-slim

# 工作目录（容器内的目录）
WORKDIR /app

# app.jar不用动，改前面的打完后的jar包名
COPY springboot-template-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口（和项目yml中的端口一致即可）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]