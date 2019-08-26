package com.company.cooChatWx.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.company.cooChatWx")
@EnableEurekaClient
@Configuration
@EnableAutoConfiguration
//@EnableRedisHttpSession
//@EnableRedisHttpSession 
@ComponentScan("com.company.cooChatWx.*")
@MapperScan({ "com.company.cooChatWx.mapper" })

public class CooChatWxApplication {
	public static void main(String[] args) {
		SpringApplication.run(CooChatWxApplication.class, args);
	}
}
