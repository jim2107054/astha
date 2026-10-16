package com.astha.notifications.controller;

import com.astha.notifications.domain.NotificationMessage;
import com.astha.notifications.service.NotificationTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NotificationTemplateService notificationService;

    public NotificationController(NotificationTemplateService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationMessage> sendNotification(@RequestBody Map<String, Object> req) {
        String recipient = (String) req.get("recipient");
        String channel = (String) req.getOrDefault("channel", "SMS");
        String templateKey = (String) req.get("templateKey");
        String language = (String) req.getOrDefault("language", "bn");
        
        @SuppressWarnings("unchecked")
        Map<String, String> params = req.containsKey("params") ? (Map<String, String>) req.get("params") : Map.of();

        return ResponseEntity.ok(notificationService.renderAndSend(recipient, channel, templateKey, language, params));
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<NotificationMessage>> getInbox() {
        return ResponseEntity.ok(notificationService.getInbox());
    }

    @GetMapping("/inbox/{recipient}")
    public ResponseEntity<List<NotificationMessage>> getInboxForRecipient(@PathVariable String recipient) {
        return ResponseEntity.ok(notificationService.getInboxForRecipient(recipient));
    }
}
