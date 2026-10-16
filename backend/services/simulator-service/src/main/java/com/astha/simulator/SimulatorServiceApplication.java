package com.astha.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.simulator", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.simulator.domain", "com.astha.platform"})
@EnableJpaRepositories(basePackages = {"com.astha.simulator.repository", "com.astha.platform"})
public class SimulatorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimulatorServiceApplication.class, args);
    }
}
