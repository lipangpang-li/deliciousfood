package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NewApplication {
    public static void main(String[] args) {
        // 支持通过命令行参数传递配置
        SpringApplication.run(NewApplication.class, args);
    }
}