package util;

import util.LogUtil;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Email service for sending OTP and other emails
 * Uses SMTP to send emails through configured email provider
 */
public class EmailService {
    
    // Email configuration - these should be moved to a config file
    private static final String SMTP_HOST = "smtp.gmail.com"; // Change to your SMTP host
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "ingabireolivier99@gmail.com"; // Change to your email
    private static final String EMAIL_PASSWORD = "uxbm cnnd tnrp txwo"; // Change to your app password
    private static final String FROM_NAME = "Business Management System";
    
    // Email templates
    private static final String OTP_SUBJECT = "Your Login Verification Code";
    private static final String OTP_TEMPLATE = 
        "Dear User,\n\n" +
        "Your verification code for Business Management System is: %s\n\n" +
        "This code will expire in %d minutes.\n\n" +
        "If you did not request this code, please ignore this email.\n\n" +
        "Best regards,\n" +
        "Business Management System Team";
    
    private static EmailService instance;
    private Properties properties;
    private Session session;
    
    /**
     * Private constructor for singleton pattern
     */
    private EmailService() {
        initializeEmailProperties();
    }
    
    /**
     * Gets the singleton instance of EmailService
     * 
     * @return EmailService instance
     */
    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }
    
    /**
     * Initializes email properties and session
     */
    private void initializeEmailProperties() {
        properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        // Create authenticator
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        };
        
        session = Session.getInstance(properties, authenticator);
        LogUtil.info("Email service initialized with SMTP host: " + SMTP_HOST);
    }
    
    /**
     * Sends an OTP email to the specified email address
     * 
     * @param toEmail The recipient email address
     * @param otpCode The OTP code to send
     * @param expiryMinutes Number of minutes until OTP expires
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendOTPEmail(String toEmail, String otpCode, int expiryMinutes) {
        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(OTP_SUBJECT);
            
            // Format email content
            String emailContent = String.format(OTP_TEMPLATE, otpCode, expiryMinutes);
            message.setText(emailContent);
            
            // Send email
            Transport.send(message);
            
            LogUtil.info("OTP email sent successfully to: " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            LogUtil.error("Failed to send OTP email to: " + toEmail, e);
            return false;
        } catch (Exception e) {
            LogUtil.error("Unexpected error sending OTP email to: " + toEmail, e);
            return false;
        }
    }
    
    /**
     * Sends an HTML OTP email with better formatting
     * 
     * @param toEmail The recipient email address
     * @param otpCode The OTP code to send
     * @param expiryMinutes Number of minutes until OTP expires
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendOTPEmailHTML(String toEmail, String otpCode, int expiryMinutes) {
        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(OTP_SUBJECT);
            
            // Create HTML content
            String htmlContent = createOTPEmailHTML(otpCode, expiryMinutes);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Send email
            Transport.send(message);
            
            LogUtil.info("HTML OTP email sent successfully to: " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            LogUtil.error("Failed to send HTML OTP email to: " + toEmail, e);
            return false;
        } catch (Exception e) {
            LogUtil.error("Unexpected error sending HTML OTP email to: " + toEmail, e);
            return false;
        }
    }
    
    /**
     * Creates HTML content for OTP email
     * 
     * @param otpCode The OTP code
     * @param expiryMinutes Expiry time in minutes
     * @return HTML email content
     */
    private String createOTPEmailHTML(String otpCode, int expiryMinutes) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='utf-8'>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
               ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
               ".header { background-color: #1976D2; color: white; padding: 20px; text-align: center; }" +
               ".content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; }" +
               ".otp-code { font-size: 32px; font-weight: bold; color: #1976D2; text-align: center; " +
               "           letter-spacing: 5px; margin: 20px 0; padding: 15px; background-color: #e3f2fd; " +
               "           border: 2px dashed #1976D2; border-radius: 8px; }" +
               ".warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; " +
               "          border-radius: 5px; margin: 20px 0; }" +
               ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h1>Business Management System</h1>" +
               "<p>Login Verification</p>" +
               "</div>" +
               "<div class='content'>" +
               "<h2>Your Verification Code</h2>" +
               "<p>Hello,</p>" +
               "<p>You have requested to log in to the Business Management System. Please use the following verification code:</p>" +
               "<div class='otp-code'>" + otpCode + "</div>" +
               "<div class='warning'>" +
               "<strong>⚠️ Important:</strong><br>" +
               "• This code will expire in <strong>" + expiryMinutes + " minutes</strong><br>" +
               "• Do not share this code with anyone<br>" +
               "• If you did not request this code, please ignore this email" +
               "</div>" +
               "<p>If you have any questions or concerns, please contact our support team.</p>" +
               "<p>Best regards,<br>Business Management System Team</p>" +
               "</div>" +
               "<div class='footer'>" +
               "<p>This is an automated message. Please do not reply to this email.</p>" +
               "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Sends a password reset email
     * 
     * @param toEmail The recipient email address
     * @param otpCode The OTP code for password reset
     * @param expiryMinutes Number of minutes until OTP expires
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendPasswordResetEmail(String toEmail, String otpCode, int expiryMinutes) {
        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset Verification Code");
            
            // Create HTML content for password reset
            String htmlContent = createPasswordResetEmailHTML(otpCode, expiryMinutes);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Send email
            Transport.send(message);
            
            LogUtil.info("Password reset email sent successfully to: " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            LogUtil.error("Failed to send password reset email to: " + toEmail, e);
            return false;
        } catch (Exception e) {
            LogUtil.error("Unexpected error sending password reset email to: " + toEmail, e);
            return false;
        }
    }
    
    /**
     * Creates HTML content for password reset email
     * 
     * @param otpCode The OTP code
     * @param expiryMinutes Expiry time in minutes
     * @return HTML email content
     */
    private String createPasswordResetEmailHTML(String otpCode, int expiryMinutes) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<meta charset='utf-8'>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
               ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
               ".header { background-color: #FF5722; color: white; padding: 20px; text-align: center; }" +
               ".content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; }" +
               ".otp-code { font-size: 32px; font-weight: bold; color: #FF5722; text-align: center; " +
               "           letter-spacing: 5px; margin: 20px 0; padding: 15px; background-color: #fbe9e7; " +
               "           border: 2px dashed #FF5722; border-radius: 8px; }" +
               ".warning { background-color: #ffebee; border: 1px solid #ffcdd2; padding: 15px; " +
               "          border-radius: 5px; margin: 20px 0; }" +
               ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h1>Business Management System</h1>" +
               "<p>Password Reset Request</p>" +
               "</div>" +
               "<div class='content'>" +
               "<h2>Reset Your Password</h2>" +
               "<p>Hello,</p>" +
               "<p>You have requested to reset your password. Please use the following verification code:</p>" +
               "<div class='otp-code'>" + otpCode + "</div>" +
               "<div class='warning'>" +
               "<strong>🔒 Security Notice:</strong><br>" +
               "• This code will expire in <strong>" + expiryMinutes + " minutes</strong><br>" +
               "• Do not share this code with anyone<br>" +
               "• If you did not request a password reset, please contact support immediately" +
               "</div>" +
               "<p>After verification, you will be able to create a new password for your account.</p>" +
               "<p>Best regards,<br>Business Management System Team</p>" +
               "</div>" +
               "<div class='footer'>" +
               "<p>This is an automated message. Please do not reply to this email.</p>" +
               "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Tests the email configuration
     * 
     * @return true if email configuration is working, false otherwise
     */
    public boolean testEmailConfiguration() {
        try {
            // Try to send a test email to the configured email address
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_USERNAME));
            message.setSubject("Email Configuration Test");
            message.setText("This is a test email to verify email configuration. " +
                           "If you receive this, the email service is working correctly.");
            
            Transport.send(message);
            LogUtil.info("Email configuration test successful");
            return true;
            
        } catch (Exception e) {
            LogUtil.error("Email configuration test failed", e);
            return false;
        }
    }
    
    /**
     * Validates email format using basic regex
     * 
     * @param email The email to validate
     * @return true if email format is valid
     */
    public boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}