package com.astha.simulator.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "synthetic_entities")
public class SyntheticEntity {

    @Id
    private String entityId;
    private String entityType; // CUSTOMER, AGENT, MERCHANT
    private String name;
    private String msisdn;
    private String district;
    private String kycTier;
    private String segment;

    public SyntheticEntity() {}

    public SyntheticEntity(String entityId, String entityType, String name, String msisdn,
                           String district, String kycTier, String segment) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.name = name;
        this.msisdn = msisdn;
        this.district = district;
        this.kycTier = kycTier;
        this.segment = segment;
    }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMsisdn() { return msisdn; }
    public void setMsisdn(String msisdn) { this.msisdn = msisdn; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getKycTier() { return kycTier; }
    public void setKycTier(String kycTier) { this.kycTier = kycTier; }

    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
}
