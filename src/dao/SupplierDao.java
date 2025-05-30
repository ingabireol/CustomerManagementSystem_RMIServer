package dao;

import model.Supplier;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.util.List;

/**
 * Data Access Object for Supplier operations using Hibernate.
 */
public class SupplierDao {
    
    /**
     * Creates a new supplier in the database
     * 
     * @param supplier The supplier to create
     * @return The created supplier with generated ID, or null if failed
     */
    public Supplier createSupplier(Supplier supplier) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(supplier);
            transaction.commit();
            LogUtil.info("Supplier created successfully: " + supplier.getSupplierCode());
            return supplier;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create supplier: " + supplier.getSupplierCode(), e);
            return null;
        }
    }
    
    /**
     * Updates an existing supplier in the database
     * 
     * @param supplier The supplier to update
     * @return The updated supplier, or null if failed
     */
    public Supplier updateSupplier(Supplier supplier) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(supplier);
            transaction.commit();
            LogUtil.info("Supplier updated successfully: " + supplier.getSupplierCode());
            return supplier;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update supplier: " + supplier.getSupplierCode(), e);
            return null;
        }
    }
    
    /**
     * Deletes a supplier from the database
     * 
     * @param supplier The supplier to delete
     * @return The deleted supplier, or null if failed
     */
    public Supplier deleteSupplier(Supplier supplier) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.delete(supplier);
            transaction.commit();
            LogUtil.info("Supplier deleted successfully: " + supplier.getSupplierCode());
            return supplier;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete supplier: " + supplier.getSupplierCode(), e);
            return null;
        }
    }
    
    /**
     * Finds a supplier by ID
     * 
     * @param id The supplier ID to search for
     * @return The supplier if found, null otherwise
     */
    public Supplier findSupplierById(int id) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Supplier supplier = (Supplier) session.get(Supplier.class, id);
            if (supplier != null) {
                LogUtil.debug("Found supplier by ID: " + id);
            } else {
                LogUtil.debug("Supplier not found with ID: " + id);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by ID: " + id, e);
            return null;
        }
    }
    
    /**
     * Finds a supplier by supplier code
     * 
     * @param supplierCode The supplier code to search for
     * @return The supplier if found, null otherwise
     */
    public Supplier findSupplierByCode(String supplierCode) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.supplierCode = :supplierCode");
            query.setParameter("supplierCode", supplierCode);
            Supplier supplier = (Supplier) query.uniqueResult();
            
            if (supplier != null) {
                LogUtil.debug("Found supplier by code: " + supplierCode);
            } else {
                LogUtil.debug("Supplier not found with code: " + supplierCode);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by code: " + supplierCode, e);
            return null;
        }
    }
    
    /**
     * Finds suppliers by name
     * 
     * @param name The name to search for
     * @return List of matching suppliers
     */
    public List<Supplier> findSuppliersByName(String name) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.name LIKE :name");
            query.setParameter("name", "%" + name + "%");
            List<Supplier> suppliers = query.list();
            LogUtil.debug("Found " + suppliers.size() + " suppliers matching name: " + name);
            return suppliers;
        } catch (Exception e) {
            LogUtil.error("Error finding suppliers by name: " + name, e);
            return null;
        }
    }
    
    /**
     * Finds a supplier by email
     * 
     * @param email The email to search for
     * @return The supplier if found, null otherwise
     */
    public Supplier findSupplierByEmail(String email) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.email = :email");
            query.setParameter("email", email);
            Supplier supplier = (Supplier) query.uniqueResult();
            
            if (supplier != null) {
                LogUtil.debug("Found supplier by email: " + email);
            } else {
                LogUtil.debug("Supplier not found with email: " + email);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by email: " + email, e);
            return null;
        }
    }
    
    /**
     * Gets all suppliers
     * 
     * @return List of all suppliers
     */
    public List<Supplier> findAllSuppliers() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Supplier ORDER BY name");
            List<Supplier> suppliers = query.list();
            LogUtil.debug("Found " + suppliers.size() + " suppliers in total");
            return suppliers;
        } catch (Exception e) {
            LogUtil.error("Error finding all suppliers", e);
            return null;
        }
    }
    
    /**
     * Gets a supplier with all their products
     * 
     * @param supplierId The ID of the supplier
     * @return The supplier with products loaded
     */
    public Supplier getSupplierWithProducts(int supplierId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s LEFT JOIN FETCH s.products WHERE s.id = :id");
            query.setParameter("id", supplierId);
            Supplier supplier = (Supplier) query.uniqueResult();
            
            if (supplier != null) {
                LogUtil.debug("Found supplier with products: " + supplierId + ", Products count: " + supplier.getProducts().size());
            } else {
                LogUtil.debug("Supplier not found with ID: " + supplierId);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier with products: " + supplierId, e);
            return null;
        }
    }
    
    /**
     * Checks if a supplier code already exists
     * 
     * @param supplierCode The supplier code to check
     * @return true if exists, false otherwise
     */
    public boolean supplierCodeExists(String supplierCode) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(s) FROM Supplier s WHERE s.supplierCode = :supplierCode");
            query.setParameter("supplierCode", supplierCode);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if supplier code exists: " + supplierCode, e);
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
                "SELECT COUNT(s) FROM Supplier s WHERE s.email = :email");
            query.setParameter("email", email);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if email exists: " + email, e);
            return false;
        }
    }
    
    /**
     * Finds suppliers by contact person
     * 
     * @param contactPerson The contact person to search for
     * @return List of matching suppliers
     */
    public List<Supplier> findSuppliersByContactPerson(String contactPerson) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.contactPerson LIKE :contactPerson");
            query.setParameter("contactPerson", "%" + contactPerson + "%");
            List<Supplier> suppliers = query.list();
            LogUtil.debug("Found " + suppliers.size() + " suppliers with contact person: " + contactPerson);
            return suppliers;
        } catch (Exception e) {
            LogUtil.error("Error finding suppliers by contact person: " + contactPerson, e);
            return null;
        }
    }
}