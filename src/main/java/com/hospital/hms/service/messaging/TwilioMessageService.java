package com.hospital.hms.service.messaging;
 
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
 
@Slf4j
@Service
public class TwilioMessageService {
 
    @Value("${twilio.account-sid}")
    private String accountSid;
 
    @Value("${twilio.auth-token}")
    private String authToken;
 
    @Value("${twilio.whatsapp-from}")
    private String whatsAppFrom;
 
    @Value("${twilio.sms-from}")
    private String smsFrom;
 
    @Value("${twilio.enabled:true}")
    private boolean enabled;
 
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("Twilio messaging is disabled (twilio.enabled=false)");
            return;
        }
        if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken)) {
            log.info("Twilio messaging is disabled because credentials are not configured");
            enabled = false;
            return;
        }
        Twilio.init(accountSid, authToken);
        log.info("Twilio messaging initialized");
    }
 
    // Converts a stored Indian 10-digit number ("9876543210") into E.164
    // format ("+919876543210") that Twilio requires. Adjust the country
    // code prefix if your hospital serves a different country.
    private String toE164(String rawPhone) {
        String digitsOnly = rawPhone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() == 10) {
            return "+91" + digitsOnly;
        }
        if (digitsOnly.startsWith("91") && digitsOnly.length() == 12) {
            return "+" + digitsOnly;
        }
        return rawPhone.startsWith("+") ? rawPhone : "+" + digitsOnly;
    }
 
    public void sendWhatsApp(String phone, String message) {
        if (!enabled) return;
        try {
            String to = "whatsapp:" + toE164(phone);
            Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(whatsAppFrom),
                message
            ).create();
            log.info("WhatsApp message sent to {}", to);
        } catch (Exception e) {
            // Fire-and-forget — a messaging failure (e.g. recipient hasn't
            // joined the sandbox yet) should never break the real action.
            log.error("Failed to send WhatsApp message to {}: {}", phone, e.getMessage());
        }
    }
 
    public void sendSms(String phone, String message) {
        if (!enabled) return;
        try {
            String to = toE164(phone);
            Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(smsFrom),
                message
            ).create();
            log.info("SMS sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phone, e.getMessage());
        }
    }
}