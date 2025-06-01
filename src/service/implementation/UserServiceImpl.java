package service.implementation;

import dao.UserDao;
import model.OTP;
import model.User;
import service.OTPService;
import service.UserService;
import util.LogUtil;
import util.OTPUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Enhanced implementation of UserService interface.
 * Handles business logic for user management and authentication operations including OTP login.
 * Fixed for PostgreSQL compatibility with proper Date handling.
 */
public class UserServiceImpl extends UnicastRemoteObject implements UserService {
    
    private UserDao userDao;
    private OTPService otpService;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public UserServiceImpl() throws RemoteException {
        super();
        this.userDao = new UserDao();
        // Initialize OTP service
        try {
            this.otpService = new OTPServiceImpl();
        } catch (RemoteException e) {
            LogUtil.error("Failed to initialize OTP service", e);
            throw e;
        }
        LogUtil.info("Enhanced UserService initialized with OTP support");
    }
    
    @Override
    public User createUser(User user) throws RemoteException {
        try {
            // Validate input
            if (user == null) {
                LogUtil.warn("Attempted to create null user");
                return null;
            }
            
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                LogUtil.warn("Attempted to create user without username");
                return null;
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                LogUtil.warn("Attempted to create user without password");
                return null;
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                LogUtil.warn("Attempted to create user without email");
                return null;
            }
            
            // Validate email format
            if (!isValidEmail(user.getEmail())) {
                LogUtil.warn("Attempted to create user with invalid email format: " + user.getEmail());
                return null;
            }
            
            // Check for existing username
            if (userDao.usernameExists(user.getUsername())) {
                LogUtil.warn("Attempted to create user with existing username: " + user.getUsername());
                return null;
            }
            
            // Check for existing email
            if (userDao.emailExists(user.getEmail())) {
                LogUtil.warn("Attempted to create user with existing email: " + user.getEmail());
                return null;
            }
            
            // Set default role if not specified
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole(User.ROLE_STAFF);
            }
            
            return userDao.createUser(user);
        } catch (Exception e) {
            LogUtil.error("Error creating user", e);
            throw new RemoteException("Failed to create user", e);
        }
    }
    
    @Override
    public User updateUser(User user) throws RemoteException {
        try {
            if (user == null || user.getId() <= 0) {
                LogUtil.warn("Attempted to update invalid user");
                return null;
            }
            
            return userDao.updateUser(user);
        } catch (Exception e) {
            LogUtil.error("Error updating user", e);
            throw new RemoteException("Failed to update user", e);
        }
    }
    
    @Override
    public int updatePassword(int userId, String newPassword) throws RemoteException {
        try {
            if (userId <= 0) {
                LogUtil.warn("Invalid user ID provided for password update: " + userId);
                return 0;
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                LogUtil.warn("Invalid password provided for user ID: " + userId);
                return 0;
            }
            
            if (newPassword.length() < 6) {
                LogUtil.warn("Password too short for user ID: " + userId);
                return 0;
            }
            
            return userDao.updatePassword(userId, newPassword);
        } catch (Exception e) {
            LogUtil.error("Error updating password for user ID: " + userId, e);
            throw new RemoteException("Failed to update password", e);
        }
    }
    
    @Override
    public User deleteUser(User user) throws RemoteException {
        try {
            if (user == null || user.getId() <= 0) {
                LogUtil.warn("Attempted to delete invalid user");
                return null;
            }
            
            // Prevent deletion of admin users if they're the last admin
            if (user.isAdmin()) {
                List<User> admins = userDao.findUsersByRole(User.ROLE_ADMIN);
                if (admins != null && admins.size() <= 1) {
                    LogUtil.warn("Attempted to delete the last admin user: " + user.getUsername());
                    return null;
                }
            }
            
            return userDao.deleteUser(user);
        } catch (Exception e) {
            LogUtil.error("Error deleting user", e);
            throw new RemoteException("Failed to delete user", e);
        }
    }
    
    @Override
    public User findUserById(int id) throws RemoteException {
        try {
            if (id <= 0) {
                LogUtil.warn("Invalid user ID provided: " + id);
                return null;
            }
            
            return userDao.findUserById(id);
        } catch (Exception e) {
            LogUtil.error("Error finding user by ID: " + id, e);
            throw new RemoteException("Failed to find user by ID", e);
        }
    }
    
    @Override
    public User findUserByUsername(String username) throws RemoteException {
        try {
            if (username == null || username.trim().isEmpty()) {
                LogUtil.warn("Invalid username provided");
                return null;
            }
            
            return userDao.findUserByUsername(username.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding user by username: " + username, e);
            throw new RemoteException("Failed to find user by username", e);
        }
    }
    
    @Override
    public User findUserByEmail(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Invalid email provided");
                return null;
            }
            
            return userDao.findUserByEmail(email.trim().toLowerCase());
        } catch (Exception e) {
            LogUtil.error("Error finding user by email: " + email, e);
            throw new RemoteException("Failed to find user by email", e);
        }
    }
    
    @Override
    public List<User> findAllUsers() throws RemoteException {
        try {
            return userDao.findAllUsers();
        } catch (Exception e) {
            LogUtil.error("Error finding all users", e);
            throw new RemoteException("Failed to find all users", e);
        }
    }
    
    @Override
    public List<User> findUsersByRole(String role) throws RemoteException {
        try {
            if (role == null || role.trim().isEmpty()) {
                LogUtil.warn("Invalid role provided for user search");
                return null;
            }
            
            return userDao.findUsersByRole(role.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding users by role: " + role, e);
            throw new RemoteException("Failed to find users by role", e);
        }
    }
    
    @Override
    public User authenticateUser(String username, String password) throws RemoteException {
        try {
            if (username == null || username.trim().isEmpty()) {
                LogUtil.warn("Authentication failed - empty username");
                return null;
            }
            
            if (password == null || password.trim().isEmpty()) {
                LogUtil.warn("Authentication failed - empty password");
                return null;
            }
            
            return userDao.authenticateUser(username.trim(), password);
        } catch (Exception e) {
            LogUtil.error("Error authenticating user: " + username, e);
            throw new RemoteException("Authentication failed", e);
        }
    }
    
    @Override
    public boolean initiateOTPLogin(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Attempted OTP login with empty email");
                return false;
            }
            
            email = email.trim().toLowerCase();
            
            // Validate email format
            if (!isValidEmail(email)) {
                LogUtil.warn("Attempted OTP login with invalid email format: " + email);
                return false;
            }
            
            // Check if user exists with this email
            User user = userDao.findUserByEmail(email);
            if (user == null) {
                LogUtil.warn("Attempted OTP login for non-existent user: " + email);
                return false;
            }
            
            if (!user.isActive()) {
                LogUtil.warn("Attempted OTP login for inactive user: " + email);
                return false;
            }
            
            // Check rate limiting
            if (isOTPRateLimited(email)) {
                LogUtil.warn("OTP login rate limited for email: " + email);
                return false;
            }
            
            // Generate and send OTP
            OTP otp = otpService.generateLoginOTP(email);
            if (otp == null) {
                LogUtil.error("Failed to generate OTP for email: " + email);
                return false;
            }
            
            LogUtil.info("OTP login initiated successfully for email: " + email);
            return true;
            
        } catch (Exception e) {
            LogUtil.error("Error initiating OTP login for email: " + email, e);
            throw new RemoteException("Failed to initiate OTP login", e);
        }
    }
    
    @Override
    public User completeOTPLogin(String email, String otpCode) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Attempted to complete OTP login with empty email");
                return null;
            }
            
            if (otpCode == null || otpCode.trim().isEmpty()) {
                LogUtil.warn("Attempted to complete OTP login with empty OTP code");
                return null;
            }
            
            email = email.trim().toLowerCase();
            otpCode = otpCode.trim();
            
            // Verify OTP
            OTP verifiedOTP = otpService.verifyLoginOTP(email, otpCode);
            if (verifiedOTP == null) {
                LogUtil.warn("Invalid OTP provided for email: " + email);
                return null;
            }
            
            // Get user
            User user = userDao.findUserByEmail(email);
            if (user == null || !user.isActive()) {
                LogUtil.warn("User not found or inactive during OTP login completion: " + email);
                return null;
            }
            
            // Update last login time
            userDao.updateLastLogin(user.getId());
            user.updateLastLogin();
            
            LogUtil.info("OTP login completed successfully for user: " + email);
            return user;
            
        } catch (Exception e) {
            LogUtil.error("Error completing OTP login for email: " + email, e);
            throw new RemoteException("Failed to complete OTP login", e);
        }
    }
    
    @Override
    public boolean initiatePasswordReset(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Attempted password reset with empty email");
                return false;
            }
            
            email = email.trim().toLowerCase();
            
            // Validate email format
            if (!isValidEmail(email)) {
                LogUtil.warn("Attempted password reset with invalid email format: " + email);
                return false;
            }
            
            // Check if user exists with this email
            User user = userDao.findUserByEmail(email);
            if (user == null) {
                LogUtil.warn("Attempted password reset for non-existent user: " + email);
                // For security, we return true even if user doesn't exist
                // This prevents email enumeration attacks
                return true;
            }
            
            if (!user.isActive()) {
                LogUtil.warn("Attempted password reset for inactive user: " + email);
                return false;
            }
            
            // Generate and send password reset OTP
            OTP otp = otpService.generateAndSendOTP(email, OTP.TYPE_PASSWORD_RESET, null, null);
            if (otp == null) {
                LogUtil.error("Failed to generate password reset OTP for email: " + email);
                return false;
            }
            
            LogUtil.info("Password reset initiated successfully for email: " + email);
            return true;
            
        } catch (Exception e) {
            LogUtil.error("Error initiating password reset for email: " + email, e);
            throw new RemoteException("Failed to initiate password reset", e);
        }
    }
    
    @Override
    public boolean verifyPasswordResetOTP(String email, String otpCode) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty() || otpCode == null || otpCode.trim().isEmpty()) {
                return false;
            }
            
            email = email.trim().toLowerCase();
            otpCode = otpCode.trim();
            
            // Verify OTP
            OTP verifiedOTP = otpService.verifyOTP(email, otpCode, OTP.TYPE_PASSWORD_RESET);
            return verifiedOTP != null;
            
        } catch (Exception e) {
            LogUtil.error("Error verifying password reset OTP for email: " + email, e);
            throw new RemoteException("Failed to verify password reset OTP", e);
        }
    }
    
    @Override
    public boolean completePasswordReset(String email, String otpCode, String newPassword) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty() || 
                otpCode == null || otpCode.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty()) {
                return false;
            }
            
            email = email.trim().toLowerCase();
            otpCode = otpCode.trim();
            
            if (newPassword.length() < 6) {
                LogUtil.warn("Password too short for password reset: " + email);
                return false;
            }
            
            // Verify OTP again for security
            OTP verifiedOTP = otpService.verifyOTP(email, otpCode, OTP.TYPE_PASSWORD_RESET);
            if (verifiedOTP == null) {
                LogUtil.warn("Invalid OTP for password reset completion: " + email);
                return false;
            }
            
            // Get user and update password
            User user = userDao.findUserByEmail(email);
            if (user == null || !user.isActive()) {
                LogUtil.warn("User not found or inactive during password reset: " + email);
                return false;
            }
            
            int result = userDao.updatePassword(user.getId(), newPassword);
            if (result > 0) {
                LogUtil.info("Password reset completed successfully for user: " + email);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            LogUtil.error("Error completing password reset for email: " + email, e);
            throw new RemoteException("Failed to complete password reset", e);
        }
    }
    
    @Override
    public boolean usernameExists(String username) throws RemoteException {
        try {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            
            return userDao.usernameExists(username.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if username exists: " + username, e);
            throw new RemoteException("Failed to check username existence", e);
        }
    }
    
    @Override
    public boolean emailExists(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            
            return userDao.emailExists(email.trim().toLowerCase());
        } catch (Exception e) {
            LogUtil.error("Error checking if email exists: " + email, e);
            throw new RemoteException("Failed to check email existence", e);
        }
    }
    
    @Override
    public User createDefaultAdmin() throws RemoteException {
        try {
            return userDao.createDefaultAdmin();
        } catch (Exception e) {
            LogUtil.error("Error creating default admin user", e);
            throw new RemoteException("Failed to create default admin user", e);
        }
    }
    
    @Override
    public boolean isValidEmail(String email) throws RemoteException {
        return OTPUtil.isValidEmail(email);
    }
    
    @Override
    public boolean isOTPRateLimited(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                return true;
            }
            
            return otpService.isRateLimited(email.trim().toLowerCase(), OTP.TYPE_LOGIN);
        } catch (Exception e) {
            LogUtil.error("Error checking OTP rate limit for email: " + email, e);
            // Return true (rate limited) on error for safety
            return true;
        }
    }
    
    @Override
    public int getOTPCooldownMinutes(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                return 0;
            }
            
            email = email.trim().toLowerCase();
            
            // Get latest OTP
            OTP latestOTP = otpService.getLatestOTP(email, OTP.TYPE_LOGIN);
            if (latestOTP == null) {
                return 0;
            }
            
            // Calculate time remaining until next OTP can be sent using Date arithmetic
            Calendar cal = Calendar.getInstance();
            cal.setTime(latestOTP.getCreatedAt());
            cal.add(Calendar.MINUTE, OTPUtil.MIN_RESEND_INTERVAL_MINUTES);
            Date nextAllowedTime = cal.getTime();
            
            Date now = new Date();
            
            if (now.after(nextAllowedTime)) {
                return 0;
            }
            
            // Calculate difference in minutes
            long diffInMillis = nextAllowedTime.getTime() - now.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            
            return (int) diffInMinutes + 1;
            
        } catch (Exception e) {
            LogUtil.error("Error getting OTP cooldown for email: " + email, e);
            throw new RemoteException("Failed to get OTP cooldown", e);
        }
    }
}