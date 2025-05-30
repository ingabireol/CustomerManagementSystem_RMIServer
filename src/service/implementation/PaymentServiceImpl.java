package service.implementation;

import dao.PaymentDao;
import model.Invoice;
import model.Payment;
import service.PaymentService;
import util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of PaymentService interface.
 * Handles business logic for payment operations.
 */
public class PaymentServiceImpl extends UnicastRemoteObject implements PaymentService {
    
    private PaymentDao paymentDao;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public PaymentServiceImpl() throws RemoteException {
        super();
        this.paymentDao = new PaymentDao();
        LogUtil.info("PaymentService initialized");
    }
    
    @Override
    public Payment createPayment(Payment payment) throws RemoteException {
        try {
            // Validate input
            if (payment == null) {
                LogUtil.warn("Attempted to create null payment");
                return null;
            }
            
            if (payment.getPaymentId() == null || payment.getPaymentId().trim().isEmpty()) {
                LogUtil.warn("Attempted to create payment without payment ID");
                return null;
            }
            
            if (payment.getInvoice() == null) {
                LogUtil.warn("Attempted to create payment without invoice");
                return null;
            }
            
            if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
                LogUtil.warn("Attempted to create payment with invalid amount");
                return null;
            }
            
            if (paymentDao.paymentIdExists(payment.getPaymentId())) {
                LogUtil.warn("Attempted to create payment with existing payment ID: " + payment.getPaymentId());
                return null;
            }
            
            return paymentDao.createPayment(payment);
        } catch (Exception e) {
            LogUtil.error("Error creating payment", e);
            throw new RemoteException("Failed to create payment", e);
        }
    }
    
    @Override
    public Payment updatePayment(Payment payment) throws RemoteException {
        try {
            if (payment == null || payment.getId() <= 0) {
                LogUtil.warn("Attempted to update invalid payment");
                return null;
            }
            
            if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
                LogUtil.warn("Attempted to update payment with invalid amount");
                return null;
            }
            
            return paymentDao.updatePayment(payment);
        } catch (Exception e) {
            LogUtil.error("Error updating payment", e);
            throw new RemoteException("Failed to update payment", e);
        }
    }
    
    @Override
    public Payment deletePayment(Payment payment) throws RemoteException {
        try {
            if (payment == null || payment.getId() <= 0) {
                LogUtil.warn("Attempted to delete invalid payment");
                return null;
            }
            
            return paymentDao.deletePayment(payment);
        } catch (Exception e) {
            LogUtil.error("Error deleting payment", e);
            throw new RemoteException("Failed to delete payment", e);
        }
    }
    
    @Override
    public Payment findPaymentById(int id) throws RemoteException {
        try {
            if (id <= 0) {
                LogUtil.warn("Invalid payment ID provided: " + id);
                return null;
            }
            
            return paymentDao.findPaymentById(id);
        } catch (Exception e) {
            LogUtil.error("Error finding payment by ID: " + id, e);
            throw new RemoteException("Failed to find payment by ID", e);
        }
    }
    
    @Override
    public Payment findPaymentByPaymentId(String paymentId) throws RemoteException {
        try {
            if (paymentId == null || paymentId.trim().isEmpty()) {
                LogUtil.warn("Invalid payment ID provided");
                return null;
            }
            
            return paymentDao.findPaymentByPaymentId(paymentId.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding payment by payment ID: " + paymentId, e);
            throw new RemoteException("Failed to find payment by payment ID", e);
        }
    }
    
    @Override
    public List<Payment> findPaymentsByInvoice(Invoice invoice) throws RemoteException {
        try {
            if (invoice == null || invoice.getId() <= 0) {
                LogUtil.warn("Invalid invoice provided for payment search");
                return null;
            }
            
            return paymentDao.findPaymentsByInvoice(invoice);
        } catch (Exception e) {
            LogUtil.error("Error finding payments by invoice: " + invoice.getId(), e);
            throw new RemoteException("Failed to find payments by invoice", e);
        }
    }
    
    @Override
    public List<Payment> findPaymentsByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException {
        try {
            if (startDate == null || endDate == null) {
                LogUtil.warn("Invalid date range provided for payment search");
                return null;
            }
            
            if (startDate.isAfter(endDate)) {
                LogUtil.warn("Start date is after end date");
                return null;
            }
            
            return paymentDao.findPaymentsByDateRange(startDate, endDate);
        } catch (Exception e) {
            LogUtil.error("Error finding payments by date range: " + startDate + " to " + endDate, e);
            throw new RemoteException("Failed to find payments by date range", e);
        }
    }
    
    @Override
    public List<Payment> findPaymentsByMethod(String paymentMethod) throws RemoteException {
        try {
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                LogUtil.warn("Invalid payment method provided for payment search");
                return null;
            }
            
            return paymentDao.findPaymentsByMethod(paymentMethod.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding payments by method: " + paymentMethod, e);
            throw new RemoteException("Failed to find payments by method", e);
        }
    }
    
    @Override
    public List<Payment> findAllPayments() throws RemoteException {
        try {
            return paymentDao.findAllPayments();
        } catch (Exception e) {
            LogUtil.error("Error finding all payments", e);
            throw new RemoteException("Failed to find all payments", e);
        }
    }
    
    @Override
    public Payment getPaymentWithInvoice(int paymentId) throws RemoteException {
        try {
            if (paymentId <= 0) {
                LogUtil.warn("Invalid payment ID provided: " + paymentId);
                return null;
            }
            
            return paymentDao.getPaymentWithInvoice(paymentId);
        } catch (Exception e) {
            LogUtil.error("Error getting payment with invoice: " + paymentId, e);
            throw new RemoteException("Failed to get payment with invoice", e);
        }
    }
    
    @Override
    public boolean paymentIdExists(String paymentId) throws RemoteException {
        try {
            if (paymentId == null || paymentId.trim().isEmpty()) {
                return false;
            }
            
            return paymentDao.paymentIdExists(paymentId.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if payment ID exists: " + paymentId, e);
            throw new RemoteException("Failed to check payment ID existence", e);
        }
    }
    
    @Override
    public List<String> findAllPaymentMethods() throws RemoteException {
        try {
            return paymentDao.findAllPaymentMethods();
        } catch (Exception e) {
            LogUtil.error("Error finding all payment methods", e);
            throw new RemoteException("Failed to find all payment methods", e);
        }
    }
}