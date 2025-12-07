package com.ecom.discoveryserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;

//This is the eureka discovery server
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication{
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }



}