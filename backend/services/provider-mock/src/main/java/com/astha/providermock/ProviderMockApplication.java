package com.astha.providermock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.providermock", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.providermock.domain", "com.astha.platform"})
@EnableJpaRepositories(basePackages = {"com.astha.providermock.repository", "com.astha.platform"})
public class ProviderMockApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderMockApplication.class, args);
    }
}
