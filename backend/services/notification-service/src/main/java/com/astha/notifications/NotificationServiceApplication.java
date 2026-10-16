package com.astha.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.notifications", "com.astha.platform"})
@EntityScan(basePackages = {"com.astha.notifications.domain", "com.astha.platform"})
@EnableJpaRepositories(basePackages = {"com.astha.notifications.repository", "com.astha.platform"})
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
