package com.astha.disputes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.disputes", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.disputes.domain", "com.astha.platform"})
@EnableJpaRepositories(basePackages = {"com.astha.disputes.repository", "com.astha.platform"})
public class DisputeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DisputeServiceApplication.class, args);
    }
}
