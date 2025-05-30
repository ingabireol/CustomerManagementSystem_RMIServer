package dao;

import model.Invoice;
import model.Payment;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for Payment operations using Hibernate.
 */
public class PaymentDao {
    
    /**
     * Creates a new payment in the database
     * 
     * @param payment The payment to create
     * @return The created payment with generated ID, or null if failed
     */
    public Payment createPayment(Payment payment) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Save the payment
            session.save(payment);
            
            // Update invoice status if needed
            Invoice invoice = payment.getInvoice();
            if (invoice != null) {
                invoice.updateStatus();
                session.update(invoice);
            }
            
            transaction.commit();
            LogUtil.info("Payment created successfully: " + payment.getPaymentId());
            return payment;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create payment: " + payment.getPaymentId(), e);
            return null;
        }
    }
    
    /**
     * Updates an existing payment in the database
     * 
     * @param payment The payment to update
     * @return The updated payment, or null if failed
     */
    public Payment updatePayment(Payment payment) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Update the payment
            session.update(payment);
            
            // Update invoice status
            Invoice invoice = payment.getInvoice();
            if (invoice != null) {
                invoice.updateStatus();
                session.update(invoice);
            }
            
            transaction.commit();
            LogUtil.info("Payment updated successfully: " + payment.getPaymentId());
            return payment;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update payment: " + payment.getPaymentId(), e);
            return null;
        }
    }
    
    /**
     * Deletes a payment from the database
     * 
     * @param payment The payment to delete
     * @return The deleted payment, or null if failed
     */
    public Payment deletePayment(Payment payment) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Delete the payment
            session.delete(payment);
            
            // Update invoice status
            Invoice invoice = payment.getInvoice();
            if (invoice != null) {
                invoice.updateStatus();
                session.update(invoice);
            }
            
            transaction.commit();
            LogUtil.info("Payment deleted successfully: " + payment.getPaymentId());
            return payment;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete payment: " + payment.getPaymentId(), e);
            return null;
        }
    }
    
    /**
     * Finds a payment by ID
     * 
     * @param id The payment ID to search for
     * @return The payment if found, null otherwise
     */
    public Payment findPaymentById(int id) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Payment payment = (Payment) session.get(Payment.class, id);
            if (payment != null) {
                LogUtil.debug("Found payment by ID: " + id);
            } else {
                LogUtil.debug("Payment not found with ID: " + id);
            }
            return payment;
        } catch (Exception e) {
            LogUtil.error("Error finding payment by ID: " + id, e);
            return null;
        }
    }
    
    /**
     * Finds a payment by payment ID
     * 
     * @param paymentId The payment ID to search for
     * @return The payment if found, null otherwise
     */
    public Payment findPaymentByPaymentId(String paymentId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Payment p WHERE p.paymentId = :paymentId");
            query.setParameter("paymentId", paymentId);
            Payment payment = (Payment) query.uniqueResult();
            
            if (payment != null) {
                LogUtil.debug("Found payment by payment ID: " + paymentId);
            } else {
                LogUtil.debug("Payment not found with payment ID: " + paymentId);
            }
            return payment;
        } catch (Exception e) {
            LogUtil.error("Error finding payment by payment ID: " + paymentId, e);
            return null;
        }
    }
    
    /**
     * Finds payments by invoice
     * 
     * @param invoice The invoice to search for
     * @return List of matching payments
     */
    public List<Payment> findPaymentsByInvoice(Invoice invoice) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Payment p WHERE p.invoice = :invoice ORDER BY p.paymentDate");
            query.setParameter("invoice", invoice);
            List<Payment> payments = query.list();
            LogUtil.debug("Found " + payments.size() + " payments for invoice: " + invoice.getInvoiceNumber());
            return payments;
        } catch (Exception e) {
            LogUtil.error("Error finding payments by invoice: " + invoice.getId(), e);
            return null;
        }
    }
    
    /**
     * Finds payments by date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of matching payments
     */
    public List<Payment> findPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC");
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Payment> payments = query.list();
            LogUtil.debug("Found " + payments.size() + " payments between " + startDate + " and " + endDate);
            return payments;
        } catch (Exception e) {
            LogUtil.error("Error finding payments by date range: " + startDate + " to " + endDate, e);
            return null;
        }
    }
    
    /**
     * Finds payments by payment method
     * 
     * @param paymentMethod The payment method to search for
     * @return List of matching payments
     */
    public List<Payment> findPaymentsByMethod(String paymentMethod) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Payment p WHERE p.paymentMethod = :paymentMethod ORDER BY p.paymentDate DESC");
            query.setParameter("paymentMethod", paymentMethod);
            List<Payment> payments = query.list();
            LogUtil.debug("Found " + payments.size() + " payments with method: " + paymentMethod);
            return payments;
        } catch (Exception e) {
            LogUtil.error("Error finding payments by method: " + paymentMethod, e);
            return null;
        }
    }
    
    /**
     * Gets all payments
     * 
     * @return List of all payments
     */
    public List<Payment> findAllPayments() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Payment ORDER BY paymentDate DESC");
            List<Payment> payments = query.list();
            LogUtil.debug("Found " + payments.size() + " payments in total");
            return payments;
        } catch (Exception e) {
            LogUtil.error("Error finding all payments", e);
            return null;
        }
    }
    
    /**
     * Gets a payment with its invoice information
     * 
     * @param paymentId The ID of the payment
     * @return The payment with invoice loaded
     */
    public Payment getPaymentWithInvoice(int paymentId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Payment p LEFT JOIN FETCH p.invoice WHERE p.id = :id");
            query.setParameter("id", paymentId);
            Payment payment = (Payment) query.uniqueResult();
            
            if (payment != null) {
                LogUtil.debug("Found payment with invoice: " + paymentId);
            } else {
                LogUtil.debug("Payment not found with ID: " + paymentId);
            }
            return payment;
        } catch (Exception e) {
            LogUtil.error("Error finding payment with invoice: " + paymentId, e);
            return null;
        }
    }
    
    /**
     * Checks if a payment ID already exists
     * 
     * @param paymentId The payment ID to check
     * @return true if exists, false otherwise
     */
    public boolean paymentIdExists(String paymentId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(p) FROM Payment p WHERE p.paymentId = :paymentId");
            query.setParameter("paymentId", paymentId);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if payment ID exists: " + paymentId, e);
            return false;
        }
    }
    
    /**
     * Gets all distinct payment methods
     * 
     * @return List of all payment methods
     */
    public List<String> findAllPaymentMethods() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT DISTINCT p.paymentMethod FROM Payment p WHERE p.paymentMethod IS NOT NULL ORDER BY p.paymentMethod");
            List<String> methods = query.list();
            LogUtil.debug("Found " + methods.size() + " distinct payment methods");
            return methods;
        } catch (Exception e) {
            LogUtil.error("Error finding all payment methods", e);
            return null;
        }
    }
}