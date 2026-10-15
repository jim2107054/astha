package com.astha.ingest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.ingest", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.ingest", "com.astha.platform.outbox"})
@EnableJpaRepositories(basePackages = {"com.astha.ingest", "com.astha.platform.outbox"})
public class IngestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IngestServiceApplication.class, args);
    }
}
