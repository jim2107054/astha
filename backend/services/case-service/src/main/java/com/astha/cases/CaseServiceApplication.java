package com.astha.cases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.cases", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.cases.domain", "com.astha.platform"})
@EnableJpaRepositories(basePackages = {"com.astha.cases.repository", "com.astha.platform"})
public class CaseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseServiceApplication.class, args);
    }
}
