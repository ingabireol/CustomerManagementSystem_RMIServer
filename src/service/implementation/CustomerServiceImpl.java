package service.implementation;

import dao.CustomerDao;
import model.Customer;
import service.CustomerService;
import util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CustomerService interface.
 * Handles business logic for customer operations.
 */
public class CustomerServiceImpl extends UnicastRemoteObject implements CustomerService {
    
    private CustomerDao customerDao;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public CustomerServiceImpl() throws RemoteException {
        super();
        this.customerDao = new CustomerDao();
        LogUtil.info("CustomerService initialized");
    }
    
    @Override
    public Customer createCustomer(Customer customer) throws RemoteException {
        try {
            // Validate input
            if (customer == null) {
                LogUtil.warn("Attempted to create null customer");
                return null;
            }
            
            if (customer.getCustomerId() == null || customer.getCustomerId().trim().isEmpty()) {
                LogUtil.warn("Attempted to create customer without customer ID");
                return null;
            }
            
            if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                if (customerDao.emailExists(customer.getEmail())) {
                    LogUtil.warn("Attempted to create customer with existing email: " + customer.getEmail());
                    return null;
                }
            }
            
            if (customerDao.customerIdExists(customer.getCustomerId())) {
                LogUtil.warn("Attempted to create customer with existing customer ID: " + customer.getCustomerId());
                return null;
            }
            customer.setOrders(new ArrayList<>(customer.getOrders()));
            return customerDao.createCustomer(customer);
        } catch (Exception e) {
            LogUtil.error("Error creating customer", e);
            throw new RemoteException("Failed to create customer", e);
        }
    }
    
    @Override
    public Customer updateCustomer(Customer customer) throws RemoteException {
        try {
            if (customer == null || customer.getId() <= 0) {
                LogUtil.warn("Attempted to update invalid customer");
                return null;
            }
             customer.setOrders(new ArrayList<>(customer.getOrders()));
            return customerDao.updateCustomer(customer);
        } catch (Exception e) {
            LogUtil.error("Error updating customer", e);
            throw new RemoteException("Failed to update customer", e);
        }
    }
    
    @Override
    public Customer deleteCustomer(Customer customer) throws RemoteException {
        try {
            if (customer == null || customer.getId() <= 0) {
                LogUtil.warn("Attempted to delete invalid customer");
                return null;
            }
            
            return customerDao.deleteCustomer(customer);
        } catch (Exception e) {
            LogUtil.error("Error deleting customer", e);
            throw new RemoteException("Failed to delete customer", e);
        }
    }
    
    @Override
    public Customer findCustomerById(int id) throws RemoteException {
        try {
            if (id <= 0) {
                LogUtil.warn("Invalid customer ID provided: " + id);
                return null;
            }
            Customer customer = customerDao.findCustomerById(id);
            customer.setOrders(new ArrayList<>(customer.getOrders()));
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by ID: " + id, e);
            throw new RemoteException("Failed to find customer by ID", e);
        }
    }
    
    @Override
    public Customer findCustomerByCustomerId(String customerId) throws RemoteException {
        try {
            if (customerId == null || customerId.trim().isEmpty()) {
                LogUtil.warn("Invalid customer ID provided");
                return null;
            }
            Customer customer = customerDao.findCustomerByCustomerId(customerId.trim());
            customer.setOrders(new ArrayList<>(customer.getOrders()));
            
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by customer ID: " + customerId, e);
            throw new RemoteException("Failed to find customer by customer ID", e);
        }
    }
    
    @Override
    public List<Customer> findCustomersByName(String name) throws RemoteException {
        try {
            if (name == null || name.trim().isEmpty()) {
                LogUtil.warn("Invalid name provided for customer search");
                return null;
            }
            
            return customerDao.findCustomersByName(name.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding customers by name: " + name, e);
            throw new RemoteException("Failed to find customers by name", e);
        }
    }
    
    @Override
    public Customer findCustomerByEmail(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                LogUtil.warn("Invalid email provided");
                return null;
            }
            Customer customer = findCustomerByEmail(email.trim());
            customer.setOrders(new ArrayList<>(customer.getOrders()));
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by email: " + email, e);
            throw new RemoteException("Failed to find customer by email", e);
        }
    }
    
    @Override
    public List<Customer> findAllCustomers() throws RemoteException {
        try {
            return customerDao.findAllCustomers();
        } catch (Exception e) {
            LogUtil.error("Error finding all customers", e);
            throw new RemoteException("Failed to find all customers", e);
        }
    }
    
    @Override
    public Customer getCustomerWithOrders(int customerId) throws RemoteException {
        try {
            if (customerId <= 0) {
                LogUtil.warn("Invalid customer ID provided: " + customerId);
                return null;
            }
            Customer customer = customerDao.getCustomerWithOrders(customerId);
            customer.setOrders(new ArrayList<>(customer.getOrders()));
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error getting customer with orders: " + customerId, e);
            throw new RemoteException("Failed to get customer with orders", e);
        }
    }
    
    @Override
    public boolean customerIdExists(String customerId) throws RemoteException {
        try {
            if (customerId == null || customerId.trim().isEmpty()) {
                return false;
            }
            
            return customerDao.customerIdExists(customerId.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if customer ID exists: " + customerId, e);
            throw new RemoteException("Failed to check customer ID existence", e);
        }
    }
    
    @Override
    public boolean emailExists(String email) throws RemoteException {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            
            return customerDao.emailExists(email.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if email exists: " + email, e);
            throw new RemoteException("Failed to check email existence", e);
        }
    }
}