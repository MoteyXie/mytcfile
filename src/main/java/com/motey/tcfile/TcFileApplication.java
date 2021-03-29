package com.motey.tcfile;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages="com.motey.tcfile.mapper")
public class TcFileApplication {

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(TcFileApplication.class);
        // 设置自定义 Banner
        //springApplication.setBanner(new BearBanner());
        // 启动 Spring Boot
        springApplication.run(args);

    }

}
