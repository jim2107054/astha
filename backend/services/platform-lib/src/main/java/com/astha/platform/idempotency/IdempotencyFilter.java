package com.astha.platform.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

public class IdempotencyFilter extends OncePerRequestFilter {
    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";
    private final IdempotencyStore store;

    public IdempotencyFilter(IdempotencyStore store) {
        this.store = store;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader(HEADER_IDEMPOTENCY_KEY);
        String method = request.getMethod();

        // Idempotency applies to state-mutating methods
        if (idempotencyKey == null || idempotencyKey.isBlank() || (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method) && !"PATCH".equalsIgnoreCase(method))) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] requestBody = request.getInputStream().readAllBytes();
        RepeatableContentRequestWrapper wrappedRequest = new RepeatableContentRequestWrapper(request, requestBody);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String requestHash = sha256(requestBody);
        Optional<IdempotencyRecord> existing = store.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(idempotencyKey,
                        "Idempotency-Key " + idempotencyKey + " was already used with a different request payload");
            }

            // Return cached response
            response.setStatus(record.getResponseStatus());
            response.setContentType("application/json");
            response.setHeader("X-Cache-Lookup", "HIT-IDEMPOTENT");
            if (record.getResponseBody() != null) {
                response.getOutputStream().write(record.getResponseBody().getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        // Store response upon completion (only for 2xx responses)
        int status = wrappedResponse.getStatus();
        if (status >= 200 && status < 300) {
            byte[] responseBytes = wrappedResponse.getContentAsByteArray();
            String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
            IdempotencyRecord record = new IdempotencyRecord(
                    idempotencyKey,
                    requestHash,
                    status,
                    responseBody,
                    Instant.now()
            );
            store.save(record);
        }
        wrappedResponse.copyBodyToResponse();
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data != null ? data : new byte[0]);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static class RepeatableContentRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final byte[] body;

        public RepeatableContentRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body != null ? body : new byte[0];
        }

        @Override
        public jakarta.servlet.ServletInputStream getInputStream() {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(body);
            return new jakarta.servlet.ServletInputStream() {
                @Override
                public boolean isFinished() { return bais.available() == 0; }
                @Override
                public boolean isReady() { return true; }
                @Override
                public void setReadListener(jakarta.servlet.ReadListener readListener) {}
                @Override
                public int read() { return bais.read(); }
            };
        }

        @Override
        public java.io.BufferedReader getReader() {
            return new java.io.BufferedReader(new java.io.InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
