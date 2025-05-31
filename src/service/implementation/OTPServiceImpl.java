package service.implementation;

import dao.OTPDao;
import dao.UserDao;
import model.OTP;
import model.User;
import service.OTPService;
import util.EmailService;
import util.LogUtil;
import util.OTPUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;

/**
 * Implementation of OTPService interface.
 * Handles business logic for OTP operations including generation, validation, and email sending.
 */
public class OTPServiceImpl extends UnicastRemoteObject implements OTPService {
    
    private OTPDao otpDao;
    private UserDao userDao;
    private EmailService emailService;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public OTPServiceImpl() throws RemoteException {
        super();
        this.otpDao = new OTPDao();
        this.userDao = new UserDao();
        this.emailService = EmailService.getInstance();
        LogUtil.info("OTPService initialized");
    }
    
    @Override
    public OTP generateAndSendOTP(String email, String otpType, String userAgent, String ipAddress) throws RemoteException {
        try {
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Attempted to generate OTP with empty email");
                return null;
            }
            
            email = email.trim().toLowerCase();
            
            if (!isValidEmail(email)) {
                LogUtil.warn("Attempted to generate OTP with invalid email format: " + email);
                return null;
            }
            
            if (otpType == null || otpType.trim().isEmpty()) {
                otpType = OTP.TYPE_LOGIN;
            }
            
            // Check rate limiting
            if (isRateLimited(email, otpType)) {
                LogUtil.warn("Rate limit exceeded for email: " + email);
                return null;
            }
            
            // For login OTPs, verify that user exists
            if (OTP.TYPE_LOGIN.equals(otpType)) {
                User user = userDao.findUserByEmail(email);
                if (user == null) {
                    LogUtil.warn("Attempted to generate login OTP for non-existent user: " + email);
                    return null;
                }
                
                if (!user.isActive()) {
                    LogUtil.warn("Attempted to generate login OTP for inactive user: " + email);
                    return null;
                }
            }
            
            // Generate OTP code
            String otpCode = OTPUtil.generateDefaultOTP();
            
            // Create OTP object
            OTP otp = new OTP(email, otpCode, otpType);
            otp.setUserAgent(userAgent);
            otp.setIpAddress(ipAddress);
            
            // Save to database
            OTP savedOTP = otpDao.createOTP(otp);
            if (savedOTP == null) {
                LogUtil.error("Failed to save OTP to database for email: " + email);
                return null;
            }
            
            // Send email
            boolean emailSent = emailService.sendOTPEmailHTML(email, otpCode, OTP.EXPIRY_MINUTES);
            if (!emailSent) {
                LogUtil.error("Failed to send OTP email to: " + email);
                // Note: We don't return null here because OTP is already saved
                // The user might try to use it even if email failed
            }
            
            LogUtil.info("OTP generated and sent successfully for email: " + email + 
                        " (OTP: " + OTPUtil.maskOTP(otpCode) + ")");
            
            return savedOTP;
            
        } catch (Exception e) {
            LogUtil.error("Error generating and sending OTP for email: " + email, e);
            throw new RemoteException("Failed to generate and send OTP", e);
        }
    }
    
    @Override
    public OTP verifyOTP(String email, String otpCode, String otpType) throws RemoteException {
        try {
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Attempted to verify OTP with empty email");
                return null;
            }
            
            if (otpCode == null || otpCode.trim().isEmpty()) {
                LogUtil.warn("Attempted to verify OTP with empty code");
                return null;
            }
            
            email = email.trim().toLowerCase();
            otpCode = otpCode.trim();
            
            if (!OTPUtil.isValidDefaultOTPFormat(otpCode)) {
                LogUtil.warn("Invalid OTP format provided for email: " + email);
                return null;
            }
            
            if (otpType == null || otpType.trim().isEmpty()) {
                otpType = OTP.TYPE_LOGIN;
            }
            
            // Find valid OTP
            OTP otp = otpDao.findValidOTP(email, otpCode, otpType);
            if (otp == null) {
                LogUtil.warn("No valid OTP found for email: " + email + " with code: " + OTPUtil.maskOTP(otpCode));
                
                // Try to find any OTP for this email to increment attempts
                OTP latestOTP = otpDao.findLatestOTPByEmail(email, otpType);
                if (latestOTP != null && !latestOTP.isUsed() && !latestOTP.isExpired()) {
                    otpDao.incrementVerificationAttempts(latestOTP.getId());
                }
                
                return null;
            }
            
            // Additional validation
            if (!otp.isValid()) {
                LogUtil.warn("Invalid OTP state for email: " + email + " - used: " + otp.isUsed() + 
                           ", expired: " + otp.isExpired() + ", attempts: " + otp.getVerificationAttempts());
                return null;
            }
            
            // Mark OTP as used
            otpDao.markOTPAsUsed(otp.getId());
            otp.markAsUsed();
            
            LogUtil.info("OTP verified successfully for email: " + email + 
                        " (OTP: " + OTPUtil.maskOTP(otpCode) + ")");
            
            return otp;
            
        } catch (Exception e) {
            LogUtil.error("Error verifying OTP for email: " + email, e);
            throw new RemoteException("Failed to verify OTP", e);
        }
    }
    
    @Override
    public OTP generateLoginOTP(String email) throws RemoteException {
        return generateAndSendOTP(email, OTP.TYPE_LOGIN, null, null);
    }
    
    @Override
    public OTP verifyLoginOTP(String email, String otpCode) throws RemoteException {
        return verifyOTP(email, otpCode, OTP.TYPE_LOGIN);
    }
    
    @Override
    public boolean isRateLimited(String email, String otpType) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                return true;
            }
            
            email = email.trim().toLowerCase();
            
            if (otpType == null || otpType.trim().isEmpty()) {
                otpType = OTP.TYPE_LOGIN;
            }
            
            // Check how many OTPs were sent in the last hour
            long otpCount = otpDao.getOTPCountLastHour(email, otpType);
            
            if (otpCount >= OTPUtil.MAX_OTP_PER_HOUR) {
                LogUtil.warn("Rate limit exceeded for email: " + email + " (count: " + otpCount + ")");
                return true;
            }
            
            // Check if minimum resend interval has passed
            OTP latestOTP = otpDao.findLatestOTPByEmail(email, otpType);
            if (latestOTP != null) {
                LocalDateTime minResendTime = latestOTP.getCreatedAt().plusMinutes(OTPUtil.MIN_RESEND_INTERVAL_MINUTES);
                if (LocalDateTime.now().isBefore(minResendTime)) {
                    LogUtil.warn("Minimum resend interval not met for email: " + email);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            LogUtil.error("Error checking rate limit for email: " + email, e);
            // Return true (rate limited) on error for safety
            return true;
        }
    }
    
    @Override
    public OTP getLatestOTP(String email, String otpType) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                return null;
            }
            
            email = email.trim().toLowerCase();
            
            if (otpType == null || otpType.trim().isEmpty()) {
                otpType = OTP.TYPE_LOGIN;
            }
            
            return otpDao.findLatestOTPByEmail(email, otpType);
            
        } catch (Exception e) {
            LogUtil.error("Error getting latest OTP for email: " + email, e);
            throw new RemoteException("Failed to get latest OTP", e);
        }
    }
    
    @Override
    public OTP resendOTP(String email, String otpType) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Attempted to resend OTP with empty email");
                return null;
            }
            
            email = email.trim().toLowerCase();
            
            if (otpType == null || otpType.trim().isEmpty()) {
                otpType = OTP.TYPE_LOGIN;
            }
            
            // Check if resend is allowed
            if (isRateLimited(email, otpType)) {
                LogUtil.warn("Resend not allowed due to rate limiting for email: " + email);
                return null;
            }
            
            // Get the latest OTP to check if it's close to expiring
            OTP latestOTP = otpDao.findLatestOTPByEmail(email, otpType);
            if (latestOTP != null && !latestOTP.isUsed() && !latestOTP.isExpired()) {
                // If the OTP still has more than 2 minutes left, don't resend
                if (latestOTP.getRemainingMinutes() > 2) {
                    LogUtil.warn("Cannot resend OTP - current OTP still valid for email: " + email);
                    return null;
                }
            }
            
            // Generate and send new OTP
            return generateAndSendOTP(email, otpType, null, null);
            
        } catch (Exception e) {
            LogUtil.error("Error resending OTP for email: " + email, e);
            throw new RemoteException("Failed to resend OTP", e);
        }
    }
    
    @Override
    public int cleanupExpiredOTPs() throws RemoteException {
        try {
            int deletedCount = otpDao.deleteExpiredOTPs();
            LogUtil.info("Cleaned up " + deletedCount + " expired OTPs");
            return deletedCount;
        } catch (Exception e) {
            LogUtil.error("Error cleaning up expired OTPs", e);
            throw new RemoteException("Failed to cleanup expired OTPs", e);
        }
    }
    
    @Override
    public boolean isValidEmail(String email) throws RemoteException {
        return OTPUtil.isValidEmail(email);
    }
}