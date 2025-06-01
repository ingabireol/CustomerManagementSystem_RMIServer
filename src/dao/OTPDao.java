package dao;

import model.OTP;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Data Access Object for OTP operations using Hibernate.
 * Fixed for PostgreSQL compatibility with proper Date handling.
 */
public class OTPDao {
    
    /**
     * Creates a new OTP in the database
     * 
     * @param otp The OTP to create
     * @return The created OTP with generated ID, or null if failed
     */
    public OTP createOTP(OTP otp) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Ensure dates are properly set
            if (otp.getCreatedAt() == null) {
                otp.setCreatedAt(new Date());
            }
            if (otp.getExpiresAt() == null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(otp.getCreatedAt());
                cal.add(Calendar.MINUTE, OTP.EXPIRY_MINUTES);
                otp.setExpiresAt(cal.getTime());
            }
            
            session.save(otp);
            transaction.commit();
            LogUtil.info("OTP created successfully for email: " + otp.getEmail());
            return otp;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create OTP for email: " + otp.getEmail(), e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Updates an existing OTP in the database
     * 
     * @param otp The OTP to update
     * @return The updated OTP, or null if failed
     */
    public OTP updateOTP(OTP otp) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(otp);
            transaction.commit();
            LogUtil.info("OTP updated successfully for email: " + otp.getEmail());
            return otp;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update OTP for email: " + otp.getEmail(), e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds an OTP by ID
     * 
     * @param id The OTP ID to search for
     * @return The OTP if found, null otherwise
     */
    public OTP findOTPById(int id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            OTP otp = (OTP) session.get(OTP.class, id);
            if (otp != null) {
                session.evict(otp);
                LogUtil.debug("Found OTP by ID: " + id);
            } else {
                LogUtil.debug("OTP not found with ID: " + id);
            }
            return otp;
        } catch (Exception e) {
            LogUtil.error("Error finding OTP by ID: " + id, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds a valid OTP by email and code
     * 
     * @param email The email address
     * @param otpCode The OTP code
     * @param otpType The type of OTP
     * @return The valid OTP if found, null otherwise
     */
    public OTP findValidOTP(String email, String otpCode, String otpType) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM OTP o WHERE o.email = :email AND o.otpCode = :otpCode AND o.otpType = :otpType " +
                "AND o.used = false AND o.expiresAt > :now AND o.verificationAttempts < :maxAttempts " +
                "ORDER BY o.createdAt DESC");
            query.setParameter("email", email);
            query.setParameter("otpCode", otpCode);
            query.setParameter("otpType", otpType);
            query.setParameter("now", new Date());
            query.setParameter("maxAttempts", OTP.MAX_VERIFICATION_ATTEMPTS);
            query.setMaxResults(1);
            
            OTP otp = (OTP) query.uniqueResult();
            if (otp != null) {
                session.evict(otp);
                LogUtil.debug("Found valid OTP for email: " + email);
            } else {
                LogUtil.debug("No valid OTP found for email: " + email + " with code: " + otpCode);
            }
            return otp;
        } catch (Exception e) {
            LogUtil.error("Error finding valid OTP for email: " + email, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds the latest OTP for an email and type
     * 
     * @param email The email address
     * @param otpType The type of OTP
     * @return The latest OTP if found, null otherwise
     */
    public OTP findLatestOTPByEmail(String email, String otpType) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM OTP o WHERE o.email = :email AND o.otpType = :otpType " +
                "ORDER BY o.createdAt DESC");
            query.setParameter("email", email);
            query.setParameter("otpType", otpType);
            query.setMaxResults(1);
            
            OTP otp = (OTP) query.uniqueResult();
            if (otp != null) {
                session.evict(otp);
                LogUtil.debug("Found latest OTP for email: " + email);
            } else {
                LogUtil.debug("No OTP found for email: " + email);
            }
            return otp;
        } catch (Exception e) {
            LogUtil.error("Error finding latest OTP for email: " + email, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds all OTPs for an email within the last hour
     * 
     * @param email The email address
     * @param otpType The type of OTP
     * @return List of recent OTPs
     */
    public List<OTP> findRecentOTPsByEmail(String email, String otpType) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            // Calculate one hour ago
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, -1);
            Date oneHourAgo = cal.getTime();
            
            Query query = session.createQuery(
                "FROM OTP o WHERE o.email = :email AND o.otpType = :otpType " +
                "AND o.createdAt > :oneHourAgo ORDER BY o.createdAt DESC");
            query.setParameter("email", email);
            query.setParameter("otpType", otpType);
            query.setParameter("oneHourAgo", oneHourAgo);
            
            List<OTP> otps = query.list();
            
            // Detach all OTPs from session
            for (OTP otp : otps) {
                session.evict(otp);
            }
            
            LogUtil.debug("Found " + otps.size() + " recent OTPs for email: " + email);
            return otps;
        } catch (Exception e) {
            LogUtil.error("Error finding recent OTPs for email: " + email, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Marks an OTP as used
     * 
     * @param otpId The ID of the OTP to mark as used
     * @return Number of rows affected
     */
    public int markOTPAsUsed(int otpId) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            Query query = session.createQuery(
                "UPDATE OTP o SET o.used = true WHERE o.id = :id");
            query.setParameter("id", otpId);
            
            int rowsAffected = query.executeUpdate();
            transaction.commit();
            
            if (rowsAffected > 0) {
                LogUtil.info("Marked OTP as used: " + otpId);
            }
            
            return rowsAffected;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to mark OTP as used: " + otpId, e);
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Increments the verification attempts for an OTP
     * 
     * @param otpId The ID of the OTP
     * @return Number of rows affected
     */
    public int incrementVerificationAttempts(int otpId) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            Query query = session.createQuery(
                "UPDATE OTP o SET o.verificationAttempts = o.verificationAttempts + 1 WHERE o.id = :id");
            query.setParameter("id", otpId);
            
            int rowsAffected = query.executeUpdate();
            transaction.commit();
            
            if (rowsAffected > 0) {
                LogUtil.debug("Incremented verification attempts for OTP: " + otpId);
            }
            
            return rowsAffected;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to increment verification attempts for OTP: " + otpId, e);
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes expired OTPs from the database (cleanup method)
     * 
     * @return Number of deleted OTPs
     */
    public int deleteExpiredOTPs() {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Delete OTPs that expired more than 24 hours ago or are used
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date oneDayAgo = cal.getTime();
            
            Query query = session.createQuery(
                "DELETE FROM OTP o WHERE o.expiresAt < :oneDayAgo OR o.used = true");
            query.setParameter("oneDayAgo", oneDayAgo);
            
            int deletedCount = query.executeUpdate();
            transaction.commit();
            
            if (deletedCount > 0) {
                LogUtil.info("Deleted " + deletedCount + " expired/used OTPs");
            }
            
            return deletedCount;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete expired OTPs", e);
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Gets count of OTPs sent to an email in the last hour (for rate limiting)
     * 
     * @param email The email address
     * @param otpType The type of OTP
     * @return Count of OTPs sent in the last hour
     */
    public long getOTPCountLastHour(String email, String otpType) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            // Calculate one hour ago
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, -1);
            Date oneHourAgo = cal.getTime();
            
            Query query = session.createQuery(
                "SELECT COUNT(o) FROM OTP o WHERE o.email = :email AND o.otpType = :otpType " +
                "AND o.createdAt > :oneHourAgo");
            query.setParameter("email", email);
            query.setParameter("otpType", otpType);
            query.setParameter("oneHourAgo", oneHourAgo);
            
            Long count = (Long) query.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            LogUtil.error("Error getting OTP count for email: " + email, e);
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}