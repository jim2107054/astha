package com.astha.notifications.service;

import com.astha.notifications.domain.NotificationMessage;
import com.astha.notifications.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NotificationTemplateService {

    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateService.class);

    private final NotificationRepository notificationRepository;

    public NotificationTemplateService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationMessage renderAndSend(String recipient, String channel, String templateKey,
                                             String language, Map<String, String> params) {
        String lang = (language != null && "en".equalsIgnoreCase(language)) ? "en" : "bn";
        String body = renderBody(templateKey, lang, params);

        NotificationMessage msg = new NotificationMessage(
                "NTF-" + UUID.randomUUID().toString().substring(0, 8),
                recipient,
                channel != null ? channel : "SMS",
                templateKey,
                lang,
                body,
                "DELIVERED"
        );

        log.info("Dispatching notification [{}] to {}: {}", templateKey, recipient, body);
        return notificationRepository.save(msg);
    }

    private String renderBody(String templateKey, String lang, Map<String, String> params) {
        String id = params.getOrDefault("id", "N/A");
        String amount = params.getOrDefault("amount", "৳০");

        if ("en".equals(lang)) {
            return switch (templateKey) {
                case "DISPUTE_FILED" -> String.format("Dear customer, your dispute (%s) has been registered. Under Bangladesh Bank guidelines, resolution is expected within 3 working days.", id);
                case "AUTO_REFUND_CREDITED" -> String.format("Your failed transaction (%s) of %s has been automatically refunded to your wallet.", id, amount);
                case "ACCOUNT_FROZEN" -> "For security protection, your account has been temporarily placed on hold. Please contact customer support.";
                case "SLA_WARNING" -> String.format("SLA Warning: Dispute %s is approaching regulatory resolution deadline.", id);
                default -> "Astha platform notification: " + id;
            };
        } else {
            return switch (templateKey) {
                case "DISPUTE_FILED" -> String.format("প্রিয় গ্রাহক, আপনার অভিযোগ (আইডি: %s) গৃহীত হয়েছে। বাংলাদেশ ব্যাংক নীতিমালা মোতাবেক ৩ কার্যদিবসের মধ্যে সমাধান নিশ্চিত করা হবে।", id);
                case "AUTO_REFUND_CREDITED" -> String.format("আপনার ব্যর্থ লেনদেনের (আইডি: %s) %s টাকা স্বয়ংক্রিয়ভাবে আপনার ওয়ালেটে রিফান্ড করা হয়েছে।", id, amount);
                case "ACCOUNT_FROZEN" -> "নিরাপত্তাজনিত কারণে আপনার অ্যাকাউন্ট সাময়িক স্থগিত রাখা হয়েছে। সহায়তার জন্য কাস্টমার কেয়ারে যোগাযোগ করুন।";
                case "SLA_WARNING" -> String.format("সতর্কতা: অভিযোগ %s এর এসএলএ সময়সীমা অতিক্রমের ঝুঁকি রয়েছে।", id);
                default -> "আস্থা প্ল্যাটফর্ম বিজ্ঞপ্তি: " + id;
            };
        }
    }

    public List<NotificationMessage> getInbox() {
        return notificationRepository.findAllByOrderBySentAtDesc();
    }

    public List<NotificationMessage> getInboxForRecipient(String recipient) {
        return notificationRepository.findByRecipientOrderBySentAtDesc(recipient);
    }
}
