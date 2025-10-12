package in.moneymanager.finguardian.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.from}") String fromEmail) {  // ✅ FIXED property name
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            System.out.println("📨 Attempting to send email...");
            System.out.println("From: " + fromEmail);
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // ✅ must be a verified sender in Brevo
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println("✅ Email successfully sent to " + to);
        } catch (Exception e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            e.printStackTrace(); // Shows full error in console for debugging
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
