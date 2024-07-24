package com.akgarg.us.apigw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGateway {

    public static void main(final String[] args) {
        SpringApplication.run(ApiGateway.class, args);
    }

}
