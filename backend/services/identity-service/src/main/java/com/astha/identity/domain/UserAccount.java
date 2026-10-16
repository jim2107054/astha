package com.astha.identity.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    private String userId;
    private String username;
    private String email;
    private String phone;
    private String role; // ANALYST, APPROVER, DISPUTE_OFFICER, RISK_MANAGER, ADMIN, CUSTOMER
    private String status; // ACTIVE, LOCKED
    private Instant createdAt;

    public UserAccount() {}

    public UserAccount(String userId, String username, String email, String phone, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
