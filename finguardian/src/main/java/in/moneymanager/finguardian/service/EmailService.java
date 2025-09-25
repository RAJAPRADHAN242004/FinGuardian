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
                        @Value("${spring.mail.properties.mail.smtp.from}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            System.out.println("Attempting to send email...");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email successfully sent to " + to);
        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
            e.printStackTrace(); // Print full error stack trace for debugging
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
