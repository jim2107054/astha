package com.astha.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiGatewayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("NFR-10 & NFR-14: Gateway health, route registry, correlation ID, and actuator")
    void shouldExposeGatewayHealthAndRoutes() throws Exception {
        // 1. Gateway custom health endpoint
        mockMvc.perform(get("/v1/gateway/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.gateway").value("Astha Unified API Gateway"));

        // 2. Gateway route directory
        mockMvc.perform(get("/v1/gateway/routes"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$['/v1/cases']").exists())
                .andExpect(jsonPath("$['/v1/disputes']").exists())
                .andExpect(jsonPath("$['/v1/analytics']").exists())
                .andExpect(jsonPath("$['/v1/notifications']").exists());

        // 3. Spring Boot Actuator Health
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // 4. Proxy invalid path returns RFC 7807 problem detail
        mockMvc.perform(get("/v1/proxy/unknown-domain/test"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Route Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
