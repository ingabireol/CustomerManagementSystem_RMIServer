package util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility class for OTP generation and validation
 */
public class OTPUtil {
    
    private static final String NUMERIC_CHARS = "0123456789";
    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();
    
    // Rate limiting constants
    public static final int MAX_OTP_PER_HOUR = 5;
    public static final int MIN_RESEND_INTERVAL_MINUTES = 2;
    
    /**
     * Generates a numeric OTP of specified length
     * 
     * @param length The length of the OTP
     * @return Generated OTP string
     */
    public static String generateNumericOTP(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be positive");
        }
        
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(NUMERIC_CHARS.charAt(random.nextInt(NUMERIC_CHARS.length())));
        }
        
        return otp.toString();
    }
    
    /**
     * Generates an alphanumeric OTP of specified length
     * 
     * @param length The length of the OTP
     * @return Generated OTP string
     */
    public static String generateAlphanumericOTP(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be positive");
        }
        
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        
        return otp.toString();
    }
    
    /**
     * Generates a default 6-digit numeric OTP
     * 
     * @return Generated 6-digit OTP
     */
    public static String generateDefaultOTP() {
        return generateNumericOTP(6);
    }
    
    /**
     * Validates OTP format (checks if it contains only allowed characters)
     * 
     * @param otp The OTP to validate
     * @param expectedLength The expected length
     * @param alphanumeric Whether alphanumeric characters are allowed
     * @return true if format is valid
     */
    public static boolean isValidOTPFormat(String otp, int expectedLength, boolean alphanumeric) {
        if (otp == null || otp.length() != expectedLength) {
            return false;
        }
        
        String allowedChars = alphanumeric ? ALPHANUMERIC_CHARS : NUMERIC_CHARS;
        for (char c : otp.toCharArray()) {
            if (allowedChars.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates a 6-digit numeric OTP format
     * 
     * @param otp The OTP to validate
     * @return true if format is valid
     */
    public static boolean isValidDefaultOTPFormat(String otp) {
        return isValidOTPFormat(otp, 6, false);
    }
    
    /**
     * Generates a simple random string for session tokens or temporary IDs
     * 
     * @param length The length of the string
     * @return Generated random string
     */
    public static String generateRandomString(int length) {
        return generateAlphanumericOTP(length);
    }
    
    /**
     * Creates a masked version of OTP for logging (shows only first and last digits)
     * 
     * @param otp The OTP to mask
     * @return Masked OTP string
     */
    public static String maskOTP(String otp) {
        if (otp == null || otp.length() < 2) {
            return "***";
        }
        
        if (otp.length() <= 4) {
            return otp.charAt(0) + "**" + otp.charAt(otp.length() - 1);
        }
        
        StringBuilder masked = new StringBuilder();
        masked.append(otp.charAt(0));
        for (int i = 1; i < otp.length() - 1; i++) {
            masked.append('*');
        }
        masked.append(otp.charAt(otp.length() - 1));
        
        return masked.toString();
    }
    
    /**
     * Validates email format using regex
     * 
     * @param email The email to validate
     * @return true if email format is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Comprehensive email validation regex
        String emailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
        return email.matches(emailRegex);
    }
    
    /**
     * Generates a secure random integer within a range
     * 
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return Random integer in range
     */
    public static int generateSecureRandomInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        return random.nextInt(max - min) + min;
    }
    
    /**
     * Generates a cryptographically secure random ID
     * 
     * @return 16-character random ID
     */
    public static String generateSecureRandomId() {
        return generateAlphanumericOTP(16);
    }
}