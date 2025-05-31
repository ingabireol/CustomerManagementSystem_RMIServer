package model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents an OTP (One Time Password) in the business management system.
 * Used for email-based authentication and login verification.
 */
@Entity
@Table(name = "otps")
public class OTP implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean used;
    
    @Column(name = "verification_attempts", nullable = false)
    private int verificationAttempts;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    // OTP types
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String TYPE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    
    @Column(name = "otp_type", length = 50)
    private String otpType;
    
    // Constants
    public static final int OTP_LENGTH = 6;
    public static final int EXPIRY_MINUTES = 10;
    public static final int MAX_VERIFICATION_ATTEMPTS = 3;
    
    /**
     * Default constructor
     */
    public OTP() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
        this.used = false;
        this.verificationAttempts = 0;
        this.otpType = TYPE_LOGIN;
    }
    
    /**
     * Constructor with essential fields
     * 
     * @param email The email address for this OTP
     * @param otpCode The generated OTP code
     * @param otpType The type of OTP (LOGIN, PASSWORD_RESET, etc.)
     */
    public OTP(String email, String otpCode, String otpType) {
        this();
        this.email = email;
        this.otpCode = otpCode;
        this.otpType = otpType;
    }
    
    /**
     * Full constructor
     * 
     * @param id Database ID
     * @param email Email address
     * @param otpCode OTP code
     * @param createdAt Creation timestamp
     * @param expiresAt Expiration timestamp
     * @param used Whether OTP has been used
     * @param verificationAttempts Number of verification attempts
     * @param otpType Type of OTP
     * @param userAgent User agent string
     * @param ipAddress IP address
     */
    public OTP(int id, String email, String otpCode, LocalDateTime createdAt, 
               LocalDateTime expiresAt, boolean used, int verificationAttempts, 
               String otpType, String userAgent, String ipAddress) {
        this.id = id;
        this.email = email;
        this.otpCode = otpCode;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = used;
        this.verificationAttempts = verificationAttempts;
        this.otpType = otpType;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getVerificationAttempts() {
        return verificationAttempts;
    }

    public void setVerificationAttempts(int verificationAttempts) {
        this.verificationAttempts = verificationAttempts;
    }

    public String getOtpType() {
        return otpType;
    }

    public void setOtpType(String otpType) {
        this.otpType = otpType;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    /**
     * Checks if the OTP is expired
     * 
     * @return true if the OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Checks if the OTP is valid (not used, not expired, and within attempt limits)
     * 
     * @return true if the OTP is valid
     */
    public boolean isValid() {
        return !used && !isExpired() && verificationAttempts < MAX_VERIFICATION_ATTEMPTS;
    }
    
    /**
     * Increments the verification attempts counter
     */
    public void incrementVerificationAttempts() {
        this.verificationAttempts++;
    }
    
    /**
     * Marks the OTP as used
     */
    public void markAsUsed() {
        this.used = true;
    }
    
    /**
     * Gets the remaining time until expiration in minutes
     * 
     * @return Minutes until expiration, or 0 if already expired
     */
    public long getRemainingMinutes() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0;
        }
        return java.time.Duration.between(now, expiresAt).toMinutes();
    }
    
    @Override
    public String toString() {
        return "OTP [id=" + id + ", email=" + email + ", type=" + otpType + 
               ", created=" + createdAt + ", expires=" + expiresAt + 
               ", used=" + used + ", attempts=" + verificationAttempts + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OTP otp = (OTP) obj;
        return id == otp.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}