package com.astha.notifications.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notification_messages")
public class NotificationMessage {

    @Id
    private String id;
    private String recipient;
    private String channel;     // SMS, EMAIL, IN_APP
    private String templateKey; // DISPUTE_FILED, AUTO_REFUND_CREDITED, ACCOUNT_FROZEN, SLA_WARNING
    private String language;    // bn, en
    private String body;
    private String status;      // DELIVERED, QUEUED
    private Instant sentAt;

    public NotificationMessage() {}

    public NotificationMessage(String id, String recipient, String channel, String templateKey,
                               String language, String body, String status) {
        this.id = id;
        this.recipient = recipient;
        this.channel = channel;
        this.templateKey = templateKey;
        this.language = language;
        this.body = body;
        this.status = status;
        this.sentAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
