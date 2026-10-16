package com.astha.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class GatewayRoutingController {

    private final RestClient restClient;

    @Value("${gateway.routes.ingest}")
    private String ingestUrl;

    @Value("${gateway.routes.risk}")
    private String riskUrl;

    @Value("${gateway.routes.graph}")
    private String graphUrl;

    @Value("${gateway.routes.case}")
    private String caseUrl;

    @Value("${gateway.routes.dispute}")
    private String disputeUrl;

    @Value("${gateway.routes.identity}")
    private String identityUrl;

    @Value("${gateway.routes.simulator}")
    private String simulatorUrl;

    @Value("${gateway.routes.notification}")
    private String notificationUrl;

    @Value("${gateway.routes.analytics}")
    private String analyticsUrl;

    @Value("${gateway.routes.provider}")
    private String providerUrl;

    public GatewayRoutingController(RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping("/v1/gateway/health")
    public ResponseEntity<Map<String, Object>> gatewayHealth() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("gateway", "Astha Unified API Gateway");
        status.put("version", "1.0.0");
        status.put("routesCount", 10);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/v1/gateway/routes")
    public ResponseEntity<Map<String, String>> getRoutes() {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("/v1/events", ingestUrl);
        routes.put("/v1/transactions", ingestUrl);
        routes.put("/v1/risk", riskUrl);
        routes.put("/v1/graph", graphUrl);
        routes.put("/v1/cases", caseUrl);
        routes.put("/v1/disputes", disputeUrl);
        routes.put("/v1/identity", identityUrl);
        routes.put("/v1/auth", identityUrl);
        routes.put("/v1/audit", identityUrl);
        routes.put("/v1/simulator", simulatorUrl);
        routes.put("/v1/notifications", notificationUrl);
        routes.put("/v1/analytics", analyticsUrl);
        routes.put("/v1/providers", providerUrl);
        return ResponseEntity.ok(routes);
    }

    @RequestMapping(value = {"/v1/proxy/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxyRequest(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String targetBaseUrl = resolveTargetBaseUrl(uri);

        if (targetBaseUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(("{\"type\":\"about:blank\",\"title\":\"Route Not Found\",\"status\":404,\"detail\":\"No registered service route for path " + uri + "\"}").getBytes());
        }

        String targetPath = uri.replace("/v1/proxy", "/v1");
        String fullUrl = targetBaseUrl + targetPath + (queryString != null ? "?" + queryString : "");

        try {
            RestClient.RequestBodySpec requestSpec = restClient
                    .method(HttpMethod.valueOf(request.getMethod()))
                    .uri(URI.create(fullUrl));

            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
                    requestSpec.header(headerName, request.getHeader(headerName));
                }
            }

            if (body != null && body.length > 0) {
                requestSpec.body(body);
            }

            return requestSpec.exchange((clientRequest, clientResponse) -> {
                HttpHeaders responseHeaders = new HttpHeaders();
                clientResponse.getHeaders().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase("transfer-encoding")) {
                        responseHeaders.put(name, values);
                    }
                });
                return ResponseEntity.status(clientResponse.getStatusCode())
                        .headers(responseHeaders)
                        .body(clientResponse.bodyTo(byte[].class));
            });
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(("{\"type\":\"about:blank\",\"title\":\"Bad Gateway\",\"status\":502,\"detail\":\"Downstream service communication failed: " + e.getMessage() + "\"}").getBytes());
        }
    }

    public String resolveTargetBaseUrl(String path) {
        if (path.contains("/events") || path.contains("/transactions")) return ingestUrl;
        if (path.contains("/risk")) return riskUrl;
        if (path.contains("/graph")) return graphUrl;
        if (path.contains("/cases")) return caseUrl;
        if (path.contains("/disputes")) return disputeUrl;
        if (path.contains("/identity") || path.contains("/auth") || path.contains("/audit")) return identityUrl;
        if (path.contains("/simulator")) return simulatorUrl;
        if (path.contains("/notifications")) return notificationUrl;
        if (path.contains("/analytics")) return analyticsUrl;
        if (path.contains("/providers")) return providerUrl;
        return null;
    }
}
