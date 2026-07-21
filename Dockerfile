# 使用官方OpenJDK 11作为基础镜像
FROM openjdk:21-slim

# 设置工作目录（容器内的目录）
WORKDIR /app

# 将本地打包好的jar包复制到容器内，并重命名为app.jar
# 注意：这里的demo-0.0.1.jar需要改成你实际打包后的文件名
COPY target/springboot-template-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口（改成你的SpringBoot应用端口，默认8080）
EXPOSE 9999

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]