package com.fzg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.fzg")
@MapperScan("com.fzg.mapper")
@EnableScheduling() // 启用定时任务
public class Website1ModuleWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(Website1ModuleWebApplication.class, args);
    }
}
