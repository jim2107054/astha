package com.astha.graph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.graph", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.graph.domain", "com.astha.platform"})
@EnableJpaRepositories(basePackages = {"com.astha.graph.repository", "com.astha.platform"})
public class GraphServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraphServiceApplication.class, args);
    }
}
