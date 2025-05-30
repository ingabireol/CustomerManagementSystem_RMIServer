package service.implementation;

import dao.InvoiceDao;
import model.Invoice;
import model.Order;
import service.InvoiceService;
import util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of InvoiceService interface.
 * Handles business logic for invoice operations.
 */
public class InvoiceServiceImpl extends UnicastRemoteObject implements InvoiceService {
    
    private InvoiceDao invoiceDao;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public InvoiceServiceImpl() throws RemoteException {
        super();
        this.invoiceDao = new InvoiceDao();
        LogUtil.info("InvoiceService initialized");
    }
    
    @Override
    public Invoice createInvoice(Invoice invoice) throws RemoteException {
        try {
            // Validate input
            if (invoice == null) {
                LogUtil.warn("Attempted to create null invoice");
                return null;
            }
            
            if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().trim().isEmpty()) {
                LogUtil.warn("Attempted to create invoice without invoice number");
                return null;
            }
            
            if (invoice.getOrder() == null) {
                LogUtil.warn("Attempted to create invoice without order");
                return null;
            }
            
            if (invoiceDao.invoiceNumberExists(invoice.getInvoiceNumber())) {
                LogUtil.warn("Attempted to create invoice with existing invoice number: " + invoice.getInvoiceNumber());
                return null;
            }
            
            return invoiceDao.createInvoice(invoice);
        } catch (Exception e) {
            LogUtil.error("Error creating invoice", e);
            throw new RemoteException("Failed to create invoice", e);
        }
    }
    
    @Override
    public Invoice updateInvoice(Invoice invoice) throws RemoteException {
        try {
            if (invoice == null || invoice.getId() <= 0) {
                LogUtil.warn("Attempted to update invalid invoice");
                return null;
            }
            
            return invoiceDao.updateInvoice(invoice);
        } catch (Exception e) {
            LogUtil.error("Error updating invoice", e);
            throw new RemoteException("Failed to update invoice", e);
        }
    }
    
    @Override
    public int updateInvoiceStatus(int invoiceId, String status) throws RemoteException {
        try {
            if (invoiceId <= 0) {
                LogUtil.warn("Invalid invoice ID provided for status update: " + invoiceId);
                return 0;
            }
            
            if (status == null || status.trim().isEmpty()) {
                LogUtil.warn("Invalid status provided for invoice update");
                return 0;
            }
            
            return invoiceDao.updateInvoiceStatus(invoiceId, status.trim());
        } catch (Exception e) {
            LogUtil.error("Error updating invoice status for ID: " + invoiceId, e);
            throw new RemoteException("Failed to update invoice status", e);
        }
    }
    
    @Override
    public Invoice deleteInvoice(Invoice invoice) throws RemoteException {
        try {
            if (invoice == null || invoice.getId() <= 0) {
                LogUtil.warn("Attempted to delete invalid invoice");
                return null;
            }
            
            return invoiceDao.deleteInvoice(invoice);
        } catch (Exception e) {
            LogUtil.error("Error deleting invoice", e);
            throw new RemoteException("Failed to delete invoice", e);
        }
    }
    
    @Override
    public Invoice findInvoiceById(int id) throws RemoteException {
        try {
            if (id <= 0) {
                LogUtil.warn("Invalid invoice ID provided: " + id);
                return null;
            }
            
            return invoiceDao.findInvoiceById(id);
        } catch (Exception e) {
            LogUtil.error("Error finding invoice by ID: " + id, e);
            throw new RemoteException("Failed to find invoice by ID", e);
        }
    }
    
    @Override
    public Invoice findInvoiceByNumber(String invoiceNumber) throws RemoteException {
        try {
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
                LogUtil.warn("Invalid invoice number provided");
                return null;
            }
            
            return invoiceDao.findInvoiceByNumber(invoiceNumber.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding invoice by number: " + invoiceNumber, e);
            throw new RemoteException("Failed to find invoice by number", e);
        }
    }
    
    @Override
    public List<Invoice> findInvoicesByOrder(Order order) throws RemoteException {
        try {
            if (order == null || order.getId() <= 0) {
                LogUtil.warn("Invalid order provided for invoice search");
                return null;
            }
            
            return invoiceDao.findInvoicesByOrder(order);
        } catch (Exception e) {
            LogUtil.error("Error finding invoices by order: " + order.getId(), e);
            throw new RemoteException("Failed to find invoices by order", e);
        }
    }
    
    @Override
    public List<Invoice> findInvoicesByStatus(String status) throws RemoteException {
        try {
            if (status == null || status.trim().isEmpty()) {
                LogUtil.warn("Invalid status provided for invoice search");
                return null;
            }
            
            return invoiceDao.findInvoicesByStatus(status.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding invoices by status: " + status, e);
            throw new RemoteException("Failed to find invoices by status", e);
        }
    }
    
    @Override
    public List<Invoice> findOverdueInvoices() throws RemoteException {
        try {
            return invoiceDao.findOverdueInvoices();
        } catch (Exception e) {
            LogUtil.error("Error finding overdue invoices", e);
            throw new RemoteException("Failed to find overdue invoices", e);
        }
    }
    
    @Override
    public List<Invoice> findInvoicesByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException {
        try {
            if (startDate == null || endDate == null) {
                LogUtil.warn("Invalid date range provided for invoice search");
                return null;
            }
            
            if (startDate.isAfter(endDate)) {
                LogUtil.warn("Start date is after end date");
                return null;
            }
            
            return invoiceDao.findInvoicesByDateRange(startDate, endDate);
        } catch (Exception e) {
            LogUtil.error("Error finding invoices by date range: " + startDate + " to " + endDate, e);
            throw new RemoteException("Failed to find invoices by date range", e);
        }
    }
    
    @Override
    public List<Invoice> findAllInvoices() throws RemoteException {
        try {
            return invoiceDao.findAllInvoices();
        } catch (Exception e) {
            LogUtil.error("Error finding all invoices", e);
            throw new RemoteException("Failed to find all invoices", e);
        }
    }
    
    @Override
    public Invoice getInvoiceWithOrder(int invoiceId) throws RemoteException {
        try {
            if (invoiceId <= 0) {
                LogUtil.warn("Invalid invoice ID provided: " + invoiceId);
                return null;
            }
            
            return invoiceDao.getInvoiceWithOrder(invoiceId);
        } catch (Exception e) {
            LogUtil.error("Error getting invoice with order: " + invoiceId, e);
            throw new RemoteException("Failed to get invoice with order", e);
        }
    }
    
    @Override
    public Invoice getInvoiceWithPayments(int invoiceId) throws RemoteException {
        try {
            if (invoiceId <= 0) {
                LogUtil.warn("Invalid invoice ID provided: " + invoiceId);
                return null;
            }
            
            return invoiceDao.getInvoiceWithPayments(invoiceId);
        } catch (Exception e) {
            LogUtil.error("Error getting invoice with payments: " + invoiceId, e);
            throw new RemoteException("Failed to get invoice with payments", e);
        }
    }
    
    @Override
    public boolean invoiceNumberExists(String invoiceNumber) throws RemoteException {
        try {
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
                return false;
            }
            
            return invoiceDao.invoiceNumberExists(invoiceNumber.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if invoice number exists: " + invoiceNumber, e);
            throw new RemoteException("Failed to check invoice number existence", e);
        }
    }
}