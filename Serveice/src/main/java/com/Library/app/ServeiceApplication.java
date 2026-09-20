package com.Library.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.Library")
@MapperScan("com.Library.mapper")
public class ServeiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServeiceApplication.class, args);
    }

}
