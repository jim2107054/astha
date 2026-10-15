package com.astha.risk;

import com.astha.platform.idempotency.IdempotencyFilter;
import com.astha.platform.idempotency.IdempotencyRecord;
import com.astha.platform.idempotency.IdempotencyStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@ComponentScan(basePackages = {"com.astha.platform", "com.astha.risk"})
@EntityScan(basePackages = {"com.astha.platform", "com.astha.risk"})
@EnableJpaRepositories(basePackages = {"com.astha.platform", "com.astha.risk"})
public class RiskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskServiceApplication.class, args);
    }

    @Bean
    public IdempotencyStore inMemoryIdempotencyStore() {
        return new IdempotencyStore() {
            private final Map<String, IdempotencyRecord> store = new ConcurrentHashMap<>();

            @Override
            public Optional<IdempotencyRecord> findByKey(String key) {
                return Optional.ofNullable(store.get(key));
            }

            @Override
            public void save(IdempotencyRecord record) {
                store.put(record.getIdempotencyKey(), record);
            }
        };
    }

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration(IdempotencyStore store) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IdempotencyFilter(store));
        registration.addUrlPatterns("/v1/*");
        registration.setOrder(1);
        return registration;
    }
}
