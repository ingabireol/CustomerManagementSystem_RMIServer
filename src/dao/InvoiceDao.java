package dao;

import model.Invoice;
import model.Order;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXED: InvoiceDao with proper RMI serialization handling
 */
public class InvoiceDao {
    
    /**
     * Creates a new invoice in the database
     * 
     * @param invoice The invoice to create
     * @return The created invoice with generated ID, or null if failed
     */
    public Invoice createInvoice(Invoice invoice) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(invoice);
            transaction.commit();
            
            // Fix RMI serialization
            if (invoice.getPayments() != null) {
                invoice.setPayments(new ArrayList<>(invoice.getPayments()));
            }
            
            LogUtil.info("Invoice created successfully: " + invoice.getInvoiceNumber());
            return invoice;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create invoice: " + invoice.getInvoiceNumber(), e);
            return null;
        }
    }
    
    /**
     * Updates an existing invoice in the database
     * 
     * @param invoice The invoice to update
     * @return The updated invoice, or null if failed
     */
    public Invoice updateInvoice(Invoice invoice) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(invoice);
            transaction.commit();
            
            // Fix RMI serialization
            if (invoice.getPayments() != null) {
                invoice.setPayments(new ArrayList<>(invoice.getPayments()));
            }
            
            LogUtil.info("Invoice updated successfully: " + invoice.getInvoiceNumber());
            return invoice;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update invoice: " + invoice.getInvoiceNumber(), e);
            return null;
        }
    }
    
    /**
     * Updates the status of an invoice
     * 
     * @param invoiceId The ID of the invoice
     * @param status The new status
     * @return Number of rows affected
     */
    public int updateInvoiceStatus(int invoiceId, String status) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            Query query = session.createQuery(
                "UPDATE Invoice i SET i.status = :status WHERE i.id = :id");
            query.setParameter("status", status);
            query.setParameter("id", invoiceId);
            int rowsAffected = query.executeUpdate();
            transaction.commit();
            LogUtil.info("Updated invoice status for invoice ID " + invoiceId + " to " + status);
            return rowsAffected;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update invoice status for invoice ID: " + invoiceId, e);
            return 0;
        }
    }
    
    /**
     * Finds an invoice by ID
     * 
     * @param id The invoice ID to search for
     * @return The invoice if found, null otherwise
     */
    public Invoice findInvoiceById(int id) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Invoice invoice = (Invoice) session.get(Invoice.class, id);
            if (invoice != null) {
                session.evict(invoice);
                // Fix RMI serialization
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
                LogUtil.debug("Found invoice by ID: " + id);
            } else {
                LogUtil.debug("Invoice not found with ID: " + id);
            }
            return invoice;
        } catch (Exception e) {
            LogUtil.error("Error finding invoice by ID: " + id, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds an invoice by invoice number
     * 
     * @param invoiceNumber The invoice number to search for
     * @return The invoice if found, null otherwise
     */
    public Invoice findInvoiceByNumber(String invoiceNumber) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Invoice i WHERE i.invoiceNumber = :invoiceNumber");
            query.setParameter("invoiceNumber", invoiceNumber);
            Invoice invoice = (Invoice) query.uniqueResult();
            
            if (invoice != null) {
                session.evict(invoice);
                // Fix RMI serialization
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
                LogUtil.debug("Found invoice by number: " + invoiceNumber);
            } else {
                LogUtil.debug("Invoice not found with number: " + invoiceNumber);
            }
            return invoice;
        } catch (Exception e) {
            LogUtil.error("Error finding invoice by number: " + invoiceNumber, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds invoices by order
     * 
     * @param order The order to search for
     * @return List of matching invoices
     */
    public List<Invoice> findInvoicesByOrder(Order order) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Invoice i WHERE i.order = :order ORDER BY i.issueDate DESC");
            query.setParameter("order", order);
            List<Invoice> invoices = query.list();
            
            // Fix RMI serialization for all invoices
            for (Invoice invoice : invoices) {
                session.evict(invoice);
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
            }
            
            LogUtil.debug("Found " + invoices.size() + " invoices for order: " + order.getOrderId());
            return invoices;
        } catch (Exception e) {
            LogUtil.error("Error finding invoices by order: " + order.getId(), e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds invoices by status
     * 
     * @param status The status to search for
     * @return List of matching invoices
     */
    public List<Invoice> findInvoicesByStatus(String status) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Invoice i WHERE i.status = :status ORDER BY i.issueDate DESC");
            query.setParameter("status", status);
            List<Invoice> invoices = query.list();
            
            // Fix RMI serialization for all invoices
            for (Invoice invoice : invoices) {
                session.evict(invoice);
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
            }
            
            LogUtil.debug("Found " + invoices.size() + " invoices with status: " + status);
            return invoices;
        } catch (Exception e) {
            LogUtil.error("Error finding invoices by status: " + status, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds invoices that are overdue
     * 
     * @return List of overdue invoices
     */
    public List<Invoice> findOverdueInvoices() {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Invoice i WHERE i.dueDate < :today AND i.status != :paid AND i.status != :cancelled ORDER BY i.dueDate");
            query.setParameter("today", LocalDate.now());
            query.setParameter("paid", Invoice.STATUS_PAID);
            query.setParameter("cancelled", Invoice.STATUS_CANCELLED);
            List<Invoice> invoices = query.list();
            
            // Fix RMI serialization for all invoices
            for (Invoice invoice : invoices) {
                session.evict(invoice);
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
            }
            
            LogUtil.debug("Found " + invoices.size() + " overdue invoices");
            return invoices;
        } catch (Exception e) {
            LogUtil.error("Error finding overdue invoices", e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Finds invoices by date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of matching invoices
     */
    public List<Invoice> findInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Invoice i WHERE i.issueDate BETWEEN :startDate AND :endDate ORDER BY i.issueDate DESC");
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Invoice> invoices = query.list();
            
            // Fix RMI serialization for all invoices
            for (Invoice invoice : invoices) {
                session.evict(invoice);
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
            }
            
            LogUtil.debug("Found " + invoices.size() + " invoices between " + startDate + " and " + endDate);
            return invoices;
        } catch (Exception e) {
            LogUtil.error("Error finding invoices by date range: " + startDate + " to " + endDate, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Gets all invoices
     * 
     * @return List of all invoices
     */
    public List<Invoice> findAllInvoices() {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Invoice ORDER BY issueDate DESC");
            List<Invoice> invoices = query.list();
            
            // Fix RMI serialization for all invoices
            for (Invoice invoice : invoices) {
                session.evict(invoice);
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
            }
            
            LogUtil.debug("Found " + invoices.size() + " invoices in total");
            return invoices;
        } catch (Exception e) {
            LogUtil.error("Error finding all invoices", e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Gets an invoice with its order information
     * 
     * @param invoiceId The ID of the invoice
     * @return The invoice with order loaded
     */
    public Invoice getInvoiceWithOrder(int invoiceId) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Invoice i LEFT JOIN FETCH i.order WHERE i.id = :id");
            query.setParameter("id", invoiceId);
            Invoice invoice = (Invoice) query.uniqueResult();
            
            if (invoice != null) {
                session.evict(invoice);
                if (invoice.getOrder() != null) {
                    session.evict(invoice.getOrder());
                }
                // Fix RMI serialization
                if (invoice.getPayments() != null) {
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
                LogUtil.debug("Found invoice with order: " + invoiceId);
            } else {
                LogUtil.debug("Invoice not found with ID: " + invoiceId);
            }
            return invoice;
        } catch (Exception e) {
            LogUtil.error("Error finding invoice with order: " + invoiceId, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Gets an invoice with its payments
     * 
     * @param invoiceId The ID of the invoice
     * @return The invoice with payments loaded
     */
    public Invoice getInvoiceWithPayments(int invoiceId) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Invoice i LEFT JOIN FETCH i.payments WHERE i.id = :id");
            query.setParameter("id", invoiceId);
            Invoice invoice = (Invoice) query.uniqueResult();
            
            if (invoice != null) {
                // Force initialization of payments collection
                invoice.getPayments().size();
                session.evict(invoice);
                
                // Detach all payments
                if (invoice.getPayments() != null) {
                    for (Object payment : invoice.getPayments()) {
                        session.evict(payment);
                    }
                    // Fix RMI serialization
                    invoice.setPayments(new ArrayList<>(invoice.getPayments()));
                }
                
                LogUtil.debug("Found invoice with payments: " + invoiceId + ", Payments count: " + invoice.getPayments().size());
            } else {
                LogUtil.debug("Invoice not found with ID: " + invoiceId);
            }
            return invoice;
        } catch (Exception e) {
            LogUtil.error("Error finding invoice with payments: " + invoiceId, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Deletes an invoice from the database
     * 
     * @param invoice The invoice to delete
     * @return The deleted invoice, or null if failed
     */
    public Invoice deleteInvoice(Invoice invoice) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Check if the invoice has payments
            if (invoice.getPayments() != null && !invoice.getPayments().isEmpty()) {
                LogUtil.warn("Cannot delete invoice with payments: " + invoice.getInvoiceNumber());
                return null;
            }
            
            session.delete(invoice);
            transaction.commit();
            
            // Fix RMI serialization
            if (invoice.getPayments() != null) {
                invoice.setPayments(new ArrayList<>(invoice.getPayments()));
            }
            
            LogUtil.info("Invoice deleted successfully: " + invoice.getInvoiceNumber());
            return invoice;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete invoice: " + invoice.getInvoiceNumber(), e);
            return null;
        }
    }
    
    /**
     * Checks if an invoice number already exists
     * 
     * @param invoiceNumber The invoice number to check
     * @return true if exists, false otherwise
     */
    public boolean invoiceNumberExists(String invoiceNumber) {
        Session session = null;
        try  {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(i) FROM Invoice i WHERE i.invoiceNumber = :invoiceNumber");
            query.setParameter("invoiceNumber", invoiceNumber);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if invoice number exists: " + invoiceNumber, e);
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}