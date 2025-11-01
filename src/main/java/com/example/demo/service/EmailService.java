package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    public void sendEmployeeCreationEmail(String toEmail, String username, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to ASMS - Set Your Password");

            String verifyLink = appUrl + "/api/auth/verify-token?token=" + token;

            String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                    "<h2 style='color: #333;'>Welcome to Automobile Management System!</h2>" +
                    "<p>Dear Employee,</p>" +
                    "<p>Your account has been created successfully.</p>" +
                    "<div style='background-color: #f5f5f5; padding: 15px; margin: 20px 0; border-left: 4px solid #007bff;'>" +
                    "<p><strong>Username:</strong> " + username + "</p>" +
                    "<p><strong>Your Token:</strong> <code style='background: #e9ecef; padding: 5px;'>" + token + "</code></p>" +
                    "</div>" +
                    "<h3>How to Set Your Password:</h3>" +
                    "<p><strong>Option 1:</strong> If you have a frontend application, click this link:</p>" +
                    "<p><a href='" + verifyLink + "' style='display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;'>Verify Account</a></p>" +
                    "<p><strong>Option 2:</strong> Use the API directly with this request:</p>" +
                    "<div style='background-color: #f8f9fa; padding: 15px; margin: 10px 0; border-radius: 5px;'>" +
                    "<p><strong>Endpoint:</strong> POST " + appUrl + "/api/auth/set-password</p>" +
                    "<p><strong>Headers:</strong> Content-Type: application/json</p>" +
                    "<p><strong>Body:</strong></p>" +
                    "<pre style='background: #e9ecef; padding: 10px; border-radius: 3px;'>" +
                    "{\n" +
                    "  \"token\": \"" + token + "\",\n" +
                    "  \"newPassword\": \"your_new_password\"\n" +
                    "}" +
                    "</pre>" +
                    "</div>" +
                    "<p style='color: #dc3545; margin-top: 20px;'><strong>Important:</strong> This link will expire in 24 hours.</p>" +
                    "<p>After setting your password, you can login with your username and new password.</p>" +
                    "<p style='margin-top: 30px;'>Best regards,<br/>ASMS Team</p>" +
                    "</div>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to: " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void sendPasswordChangedEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Changed Successfully");

            String htmlContent = "<html><body>" +
                    "<h2>Password Changed</h2>" +
                    "<p>Dear " + username + ",</p>" +
                    "<p>Your password has been changed successfully.</p>" +
                    "<p>If you did not make this change, please contact the administrator immediately.</p>" +
                    "<p>Best regards,<br/>ASMS Team</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("Failed to send password change email to: " + toEmail);
            e.printStackTrace();
        }
    }
}


