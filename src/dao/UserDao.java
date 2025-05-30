package dao;

import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;
import util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object for User operations using Hibernate.
 * Follows the same pattern as other DAOs but includes security features.
 */
public class UserDao {
    
    /**
     * Creates a new user in the database
     * 
     * @param user The user to create
     * @return The created user with generated ID, or null if failed
     */
    public User createUser(User user) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Hash the password if it's not already hashed
            if (user.getSalt() == null) {
                String salt = SecurityUtil.generateSaltString();
                String hashedPassword = SecurityUtil.hashPasswordString(user.getPassword(), salt);
                user.setSalt(salt);
                user.setPassword(hashedPassword);
            }
            
            session.save(user);
            transaction.commit();
            LogUtil.info("User created successfully: " + user.getUsername());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create user: " + user.getUsername(), e);
            return null;
        }
    }
    
    /**
     * Updates an existing user in the database
     * 
     * @param user The user to update
     * @return The updated user, or null if failed
     */
    public User updateUser(User user) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
            LogUtil.info("User updated successfully: " + user.getUsername());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update user: " + user.getUsername(), e);
            return null;
        }
    }
    
    /**
     * Updates a user's password
     * 
     * @param userId The ID of the user
     * @param newPassword The new password (plaintext)
     * @return Number of rows affected
     */
    public int updatePassword(int userId, String newPassword) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Generate new salt and hash
            String salt = SecurityUtil.generateSaltString();
            String hashedPassword = SecurityUtil.hashPasswordString(newPassword, salt);
            
            Query query = session.createQuery(
                "UPDATE User u SET u.password = :password, u.salt = :salt WHERE u.id = :id");
            query.setParameter("password", hashedPassword);
            query.setParameter("salt", salt);
            query.setParameter("id", userId);
            
            int rowsAffected = query.executeUpdate();
            transaction.commit();
            LogUtil.info("Password updated for user ID: " + userId);
            return rowsAffected;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update password for user ID: " + userId, e);
            return 0;
        }
    }
    
    /**
     * Updates a user's last login time
     * 
     * @param userId The ID of the user
     * @return Number of rows affected
     */
    public int updateLastLogin(int userId) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            Query query = session.createQuery(
                "UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :id");
            query.setParameter("lastLogin", LocalDateTime.now());
            query.setParameter("id", userId);
            int rowsAffected = query.executeUpdate();
            transaction.commit();
            return rowsAffected;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update last login for user ID: " + userId, e);
            return 0;
        }
    }
    
    /**
     * Finds a user by ID
     * 
     * @param id The user ID to search for
     * @return The user if found, null otherwise
     */
    public User findUserById(int id) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            User user = (User) session.get(User.class, id);
            if (user != null) {
                LogUtil.debug("Found user by ID: " + id);
            } else {
                LogUtil.debug("User not found with ID: " + id);
            }
            return user;
        } catch (Exception e) {
            LogUtil.error("Error finding user by ID: " + id, e);
            return null;
        }
    }
    
    /**
     * Finds a user by username
     * 
     * @param username The username to search for
     * @return The user if found, null otherwise
     */
    public User findUserByUsername(String username) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM User u WHERE u.username = :username");
            query.setParameter("username", username);
            User user = (User) query.uniqueResult();
            
            if (user != null) {
                LogUtil.debug("Found user by username: " + username);
            } else {
                LogUtil.debug("User not found with username: " + username);
            }
            return user;
        } catch (Exception e) {
            LogUtil.error("Error finding user by username: " + username, e);
            return null;
        }
    }
    
    /**
     * Finds a user by email
     * 
     * @param email The email to search for
     * @return The user if found, null otherwise
     */
    public User findUserByEmail(String email) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM User u WHERE u.email = :email");
            query.setParameter("email", email);
            User user = (User) query.uniqueResult();
            
            if (user != null) {
                LogUtil.debug("Found user by email: " + email);
            } else {
                LogUtil.debug("User not found with email: " + email);
            }
            return user;
        } catch (Exception e) {
            LogUtil.error("Error finding user by email: " + email, e);
            return null;
        }
    }
    
    /**
     * Gets all users
     * 
     * @return List of all users
     */
    public List<User> findAllUsers() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User ORDER BY username");
            List<User> users = query.list();
            LogUtil.debug("Found " + users.size() + " users in total");
            return users;
        } catch (Exception e) {
            LogUtil.error("Error finding all users", e);
            return null;
        }
    }
    
    /**
     * Finds users by role
     * 
     * @param role The role to search for
     * @return List of matching users
     */
    public List<User> findUsersByRole(String role) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM User u WHERE u.role = :role ORDER BY u.username");
            query.setParameter("role", role);
            List<User> users = query.list();
            LogUtil.debug("Found " + users.size() + " users with role: " + role);
            return users;
        } catch (Exception e) {
            LogUtil.error("Error finding users by role: " + role, e);
            return null;
        }
    }
    
    /**
     * Authenticates a user with username and password
     * 
     * @param username The username
     * @param password The password (plaintext)
     * @return The authenticated user if successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        try {
            // Find the user by username
            User user = findUserByUsername(username);
            if (user == null) {
                LogUtil.warn("Authentication failed - user not found: " + username);
                return null;
            }
            
            // Check if user is active
            if (!user.isActive()) {
                LogUtil.warn("Authentication failed - user inactive: " + username);
                return null;
            }
            
            // Verify the password
            if (SecurityUtil.verifyPasswordString(password, user.getPassword(), user.getSalt())) {
                // Update last login time
                updateLastLogin(user.getId());
                user.updateLastLogin();
                LogUtil.info("User authenticated successfully: " + username);
                return user;
            } else {
                LogUtil.warn("Authentication failed - invalid password: " + username);
                return null;
            }
            
        } catch (Exception e) {
            LogUtil.error("Authentication error for user: " + username, e);
            return null;
        }
    }
    
    /**
     * Deletes a user from the database
     * 
     * @param user The user to delete
     * @return The deleted user, or null if failed
     */
    public User deleteUser(User user) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.delete(user);
            transaction.commit();
            LogUtil.info("User deleted successfully: " + user.getUsername());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete user: " + user.getUsername(), e);
            return null;
        }
    }
    
    /**
     * Checks if a username already exists
     * 
     * @param username The username to check
     * @return true if exists, false otherwise
     */
    public boolean usernameExists(String username) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username");
            query.setParameter("username", username);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if username exists: " + username, e);
            return false;
        }
    }
    
    /**
     * Checks if an email already exists
     * 
     * @param email The email to check
     * @return true if exists, false otherwise
     */
    public boolean emailExists(String email) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email");
            query.setParameter("email", email);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if email exists: " + email, e);
            return false;
        }
    }
    
    /**
     * Creates a default admin user if no users exist
     * 
     * @return The created admin user, or null if failed or already exists
     */
    public User createDefaultAdmin() {
        try {
            // Check if there are any users
            List<User> users = findAllUsers();
            if (users != null && !users.isEmpty()) {
                LogUtil.debug("Users already exist, skipping default admin creation");
                return null;
            }
            
            // Create default admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // This will be hashed in createUser
            admin.setFullName("System Administrator");
            admin.setEmail("admin@example.com");
            admin.setRole(User.ROLE_ADMIN);
            admin.setActive(true);
            
            User result = createUser(admin);
            if (result != null) {
                LogUtil.info("Default admin user created successfully");
            }
            
            return result;
            
        } catch (Exception e) {
            LogUtil.error("Failed to create default admin user", e);
            return null;
        }
    }
}