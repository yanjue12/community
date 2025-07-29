# 第一阶段：构建
FROM maven:3.9.6-eclipse-temurin-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:8-jre-jammy
LABEL authors="yanjue"

# 时区配置
ENV TZ=Asia/Shanghai
# 修正：eclipse-temurin:8-jre-jammy 基于 Ubuntu，使用 apt 而非 apk
RUN apt-get update && apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/website1-module-web/target/website1-module-web-0.0.1-SNAPSHOT.jar /app/website.jar


EXPOSE 9322

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-jar", "website.jar"]