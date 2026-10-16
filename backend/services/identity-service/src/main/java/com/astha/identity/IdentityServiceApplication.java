package com.astha.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.identity", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.identity.domain", "com.astha.platform"})
@EnableJpaRepositories(basePackages = {"com.astha.identity.repository", "com.astha.platform"})
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
