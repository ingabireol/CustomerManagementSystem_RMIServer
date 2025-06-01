package dao;

import java.util.ArrayList;
import model.Customer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.util.List;

/**
 * FIXED: CustomerDao with proper RMI serialization handling
 */
public class CustomerDao {
    
    public Customer createCustomer(Customer customer) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(customer);
            transaction.commit();
            LogUtil.info("Customer created successfully: " + customer.getCustomerId());
            customer.setOrders(new ArrayList<>(customer.getOrders()));
            return customer;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create customer: " + customer.getCustomerId(), e);
            return null;
        }
    }
    
    public Customer updateCustomer(Customer customer) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(customer);
            transaction.commit();
            LogUtil.info("Customer updated successfully: " + customer.getCustomerId());
            return customer;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update customer: " + customer.getCustomerId(), e);
            return null;
        }
    }
    
    public Customer deleteCustomer(Customer customer) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.delete(customer);
            transaction.commit();
            LogUtil.info("Customer deleted successfully: " + customer.getCustomerId());
            customer.setOrders(new ArrayList<>(customer.getOrders()));
            return customer;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete customer: " + customer.getCustomerId(), e);
            return null;
        }
    }
    
    public Customer findCustomerById(int id) {
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Customer customer = (Customer) session.get(Customer.class, id);
            if (customer != null) {
                // Initialize for RMI serialization - detach from session
                session.evict(customer);
                LogUtil.debug("Found customer by ID: " + id);
            } else {
                LogUtil.debug("Customer not found with ID: " + id);
            }
            session.close();
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by ID: " + id, e);
            return null;
        }
    }
    
    public Customer findCustomerByCustomerId(String customerId) {
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer c WHERE c.customerId = :customerId");
            query.setParameter("customerId", customerId);
            Customer customer = (Customer) query.uniqueResult();
            
            if (customer != null) {
                session.evict(customer); // Detach for RMI
                LogUtil.debug("Found customer by customer ID: " + customerId);
            } else {
                LogUtil.debug("Customer not found with customer ID: " + customerId);
            }
            session.close();
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by customer ID: " + customerId, e);
            return null;
        }
    }
    
    public List<Customer> findCustomersByName(String name) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer c WHERE c.firstName LIKE :name OR c.lastName LIKE :name OR " +
                "CONCAT(c.firstName, ' ', c.lastName) LIKE :name");
            query.setParameter("name", "%" + name + "%");
            List<Customer> customers = query.list();
            
            // Detach all customers from session for RMI serialization
            for (Customer customer : customers) {
                customer.setOrders(new ArrayList<>(customer.getOrders()));
                session.evict(customer);
            }
            
            LogUtil.debug("Found " + customers.size() + " customers matching name: " + name);
            return customers;
        } catch (Exception e) {
            LogUtil.error("Error finding customers by name: " + name, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Customer findCustomerByEmail(String email) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer c WHERE c.email = :email");
            query.setParameter("email", email);
            Customer customer = (Customer) query.uniqueResult();
            
            if (customer != null) {
                session.evict(customer);
                LogUtil.debug("Found customer by email: " + email);
            } else {
                LogUtil.debug("Customer not found with email: " + email);
            }
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by email: " + email, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Customer> findAllCustomers() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Customer ORDER BY firstName, lastName");
            List<Customer> customers = query.list();
            
            // Detach all customers from session
            for (Customer customer : customers) {
                customer.setOrders(new ArrayList<>(customer.getOrders()));
                session.evict(customer);
            }
            
            LogUtil.debug("Found " + customers.size() + " customers in total");
            return customers;
        } catch (Exception e) {
            LogUtil.error("Error finding all customers", e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Customer getCustomerWithOrders(int customerId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            // Use explicit join to avoid lazy loading issues
            Query query = session.createQuery(
                "SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = :id");
            query.setParameter("id", customerId);
            Customer customer = (Customer) query.uniqueResult();
            
            if (customer != null) {
                // Force initialization of orders collection
                customer.getOrders().size();
                session.evict(customer);
                // Also evict orders to prevent proxy issues
                if (customer.getOrders() != null) {
                    for (Object order : customer.getOrders()) {
                        session.evict(order);
                    }
                }
                LogUtil.debug("Found customer with orders: " + customerId + 
                             ", Orders count: " + customer.getOrders().size());
            } else {
                LogUtil.debug("Customer not found with ID: " + customerId);
            }
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer with orders: " + customerId, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public boolean customerIdExists(String customerId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.customerId = :customerId");
            query.setParameter("customerId", customerId);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if customer ID exists: " + customerId, e);
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public boolean emailExists(String email) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.email = :email");
            query.setParameter("email", email);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if email exists: " + email, e);
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}