package com.astha.platform.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * RFC 9457 compliant Problem Details representation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetails {
    private URI type;
    private String title;
    private int status;
    private String detail;
    private URI instance;
    private Instant timestamp;
    private List<InvalidParam> invalidParams;
    private Map<String, Object> extensions;

    public ProblemDetails() {
        this.timestamp = Instant.now();
    }

    public ProblemDetails(URI type, String title, int status, String detail, URI instance) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.timestamp = Instant.now();
    }

    public static ProblemDetails forStatus(int status, String title, String detail) {
        return new ProblemDetails(
            URI.create("https://astha.fintech.bd/errors/" + status),
            title,
            status,
            detail,
            null
        );
    }

    public URI getType() { return type; }
    public void setType(URI type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public URI getInstance() { return instance; }
    public void setInstance(URI instance) { this.instance = instance; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<InvalidParam> getInvalidParams() { return invalidParams; }
    public void setInvalidParams(List<InvalidParam> invalidParams) { this.invalidParams = invalidParams; }

    public Map<String, Object> getExtensions() { return extensions; }
    public void setExtensions(Map<String, Object> extensions) { this.extensions = extensions; }

    public record InvalidParam(String name, String reason) {}
}
