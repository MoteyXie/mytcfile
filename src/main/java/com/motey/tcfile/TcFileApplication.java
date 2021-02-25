package com.motey.tcfile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TcFileApplication {

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(TcFileApplication.class);
        // 设置自定义 Banner
        //springApplication.setBanner(new BearBanner());
        // 启动 Spring Boot
        springApplication.run(args);

    }

}
