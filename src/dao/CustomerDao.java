package dao;

import model.Customer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.util.List;

/**
 * Data Access Object for Customer operations using Hibernate.
 */
public class CustomerDao {
    
    /**
     * Creates a new customer in the database
     * 
     * @param customer The customer to create
     * @return The created customer with generated ID, or null if failed
     */
    public Customer createCustomer(Customer customer) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(customer);
            transaction.commit();
            LogUtil.info("Customer created successfully: " + customer.getCustomerId());
            return customer;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create customer: " + customer.getCustomerId(), e);
            return null;
        }
    }
    
    /**
     * Updates an existing customer in the database
     * 
     * @param customer The customer to update
     * @return The updated customer, or null if failed
     */
    public Customer updateCustomer(Customer customer) {
        Transaction transaction = null;
        try  {
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
    
    /**
     * Deletes a customer from the database
     * 
     * @param customer The customer to delete
     * @return The deleted customer, or null if failed
     */
    public Customer deleteCustomer(Customer customer) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.delete(customer);
            transaction.commit();
            LogUtil.info("Customer deleted successfully: " + customer.getCustomerId());
            return customer;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete customer: " + customer.getCustomerId(), e);
            return null;
        }
    }
    
    /**
     * Finds a customer by ID
     * 
     * @param id The customer ID to search for
     * @return The customer if found, null otherwise
     */
    public Customer findCustomerById(int id) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Customer customer = (Customer) session.get(Customer.class, id);
            if (customer != null) {
                LogUtil.debug("Found customer by ID: " + id);
            } else {
                LogUtil.debug("Customer not found with ID: " + id);
            }
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by ID: " + id, e);
            return null;
        }
    }
    
    /**
     * Finds a customer by their customer ID
     * 
     * @param customerId The customer ID to search for
     * @return The customer if found, null otherwise
     */
    public Customer findCustomerByCustomerId(String customerId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer c WHERE c.customerId = :customerId");
            query.setParameter("customerId", customerId);
            Customer customer = (Customer) query.uniqueResult();
            
            if (customer != null) {
                LogUtil.debug("Found customer by customer ID: " + customerId);
            } else {
                LogUtil.debug("Customer not found with customer ID: " + customerId);
            }
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by customer ID: " + customerId, e);
            return null;
        }
    }
    
    /**
     * Finds customers by name (first name, last name, or full name)
     * 
     * @param name The name to search for
     * @return List of matching customers
     */
    public List<Customer> findCustomersByName(String name) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer c WHERE c.firstName LIKE :name OR c.lastName LIKE :name OR " +
                "CONCAT(c.firstName, ' ', c.lastName) LIKE :name");
            query.setParameter("name", "%" + name + "%");
            List<Customer> customers = query.list();
            LogUtil.debug("Found " + customers.size() + " customers matching name: " + name);
            return customers;
        } catch (Exception e) {
            LogUtil.error("Error finding customers by name: " + name, e);
            return null;
        }
    }
    
    /**
     * Finds a customer by email
     * 
     * @param email The email to search for
     * @return The customer if found, null otherwise
     */
    public Customer findCustomerByEmail(String email) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer c WHERE c.email = :email");
            query.setParameter("email", email);
            Customer customer = (Customer) query.uniqueResult();
            
            if (customer != null) {
                LogUtil.debug("Found customer by email: " + email);
            } else {
                LogUtil.debug("Customer not found with email: " + email);
            }
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer by email: " + email, e);
            return null;
        }
    }
    
    /**
     * Gets all customers
     * 
     * @return List of all customers
     */
    public List<Customer> findAllCustomers() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Customer ORDER BY firstName, lastName");
            List<Customer> customers = query.list();
            LogUtil.debug("Found " + customers.size() + " customers in total");
            return customers;
        } catch (Exception e) {
            LogUtil.error("Error finding all customers", e);
            return null;
        }
    }
    
    /**
     * Gets a customer with all their orders (using LEFT JOIN FETCH to avoid lazy loading issues)
     * 
     * @param customerId The ID of the customer
     * @return The customer with orders loaded
     */
    public Customer getCustomerWithOrders(int customerId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = :id");
            query.setParameter("id", customerId);
            Customer customer = (Customer) query.uniqueResult();
            
            if (customer != null) {
                LogUtil.debug("Found customer with orders: " + customerId + ", Orders count: " + customer.getOrders().size());
            } else {
                LogUtil.debug("Customer not found with ID: " + customerId);
            }
            return customer;
        } catch (Exception e) {
            LogUtil.error("Error finding customer with orders: " + customerId, e);
            return null;
        }
    }
    
    /**
     * Checks if a customer ID already exists
     * 
     * @param customerId The customer ID to check
     * @return true if exists, false otherwise
     */
    public boolean customerIdExists(String customerId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.customerId = :customerId");
            query.setParameter("customerId", customerId);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if customer ID exists: " + customerId, e);
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
                "SELECT COUNT(c) FROM Customer c WHERE c.email = :email");
            query.setParameter("email", email);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if email exists: " + email, e);
            return false;
        }
    }
}