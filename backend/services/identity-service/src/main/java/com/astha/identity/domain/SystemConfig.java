package com.astha.identity.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "system_configs")
public class SystemConfig {

    @Id
    private String configKey;
    private String configValue;
    private String description;
    private int version;
    private String updatedBy;
    private Instant updatedAt;

    public SystemConfig() {}

    public SystemConfig(String configKey, String configValue, String description, int version, String updatedBy) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        this.version = version;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
