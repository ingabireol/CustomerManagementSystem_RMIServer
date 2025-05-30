package service.implementation;

import dao.UserDao;
import model.User;
import service.UserService;
import util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementation of UserService interface.
 * Handles business logic for user management and authentication operations.
 */
public class UserServiceImpl extends UnicastRemoteObject implements UserService {
    
    private UserDao userDao;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public UserServiceImpl() throws RemoteException {
        super();
        this.userDao = new UserDao();
        LogUtil.info("UserService initialized");
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
            
            return userDao.findUserByEmail(email.trim());
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
            
            return userDao.emailExists(email.trim());
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
}