package util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility for password hashing and verification
 */
public class SecurityUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 32;
    
    /**
     * Generates a random salt string
     * 
     * @return Base64 encoded salt string
     */
    public static String generateSaltString() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            return Base64.getEncoder().encodeToString(salt);
        } catch (Exception e) {
            LogUtil.error("Failed to generate salt", e);
            return null;
        }
    }
    
    /**
     * Hashes a password with salt
     * 
     * @param password The plain text password
     * @param salt The salt string
     * @return Base64 encoded hash string
     */
    public static String hashPasswordString(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Combine password and salt
            String saltedPassword = password + salt;
            
            // Hash the combined string
            byte[] hashedBytes = digest.digest(saltedPassword.getBytes("UTF-8"));
            
            // Return as Base64 string
            return Base64.getEncoder().encodeToString(hashedBytes);
            
        } catch (Exception e) {
            LogUtil.error("Failed to hash password", e);
            return null;
        }
    }
    
    /**
     * Verifies a password against a hash
     * 
     * @param password The plain text password to verify
     * @param hash The stored hash
     * @param salt The stored salt
     * @return true if password matches
     */
    public static boolean verifyPasswordString(String password, String hash, String salt) {
        try {
            String computedHash = hashPasswordString(password, salt);
            return computedHash != null && computedHash.equals(hash);
        } catch (Exception e) {
            LogUtil.error("Failed to verify password", e);
            return false;
        }
    }
}