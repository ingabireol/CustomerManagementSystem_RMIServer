package dao;

import model.Supplier;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * FIXED: SupplierDao with proper RMI serialization handling
 */
public class SupplierDao {
    
    public Supplier createSupplier(Supplier supplier) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(supplier);
            transaction.commit();
            LogUtil.info("Supplier created successfully: " + supplier.getSupplierCode());
            // Fix RMI serialization
            if (supplier.getProducts() != null) {
                supplier.setProducts(new ArrayList<>(supplier.getProducts()));
            }
            return supplier;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create supplier: " + supplier.getSupplierCode(), e);
            return null;
        }
    }
    
    public Supplier updateSupplier(Supplier supplier) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(supplier);
            transaction.commit();
            LogUtil.info("Supplier updated successfully: " + supplier.getSupplierCode());
            // Fix RMI serialization
            if (supplier.getProducts() != null) {
                supplier.setProducts(new ArrayList<>(supplier.getProducts()));
            }
            return supplier;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update supplier: " + supplier.getSupplierCode(), e);
            return null;
        }
    }
    
    public Supplier deleteSupplier(Supplier supplier) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.delete(supplier);
            transaction.commit();
            LogUtil.info("Supplier deleted successfully: " + supplier.getSupplierCode());
            // Fix RMI serialization
            if (supplier.getProducts() != null) {
                supplier.setProducts(new ArrayList<>(supplier.getProducts()));
            }
            return supplier;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete supplier: " + supplier.getSupplierCode(), e);
            return null;
        }
    }
    
    public Supplier findSupplierById(int id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Supplier supplier = (Supplier) session.get(Supplier.class, id);
            if (supplier != null) {
                session.evict(supplier); // Detach for RMI
                // Fix RMI serialization - convert Hibernate collection to ArrayList
                if (supplier.getProducts() != null) {
                    supplier.setProducts(new ArrayList<>(supplier.getProducts()));
                }
                LogUtil.debug("Found supplier by ID: " + id);
            } else {
                LogUtil.debug("Supplier not found with ID: " + id);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by ID: " + id, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Supplier findSupplierByCode(String supplierCode) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.supplierCode = :supplierCode");
            query.setParameter("supplierCode", supplierCode);
            Supplier supplier = (Supplier) query.uniqueResult();
            
            if (supplier != null) {
                session.evict(supplier);
                // Fix RMI serialization
                if (supplier.getProducts() != null) {
                    supplier.setProducts(new ArrayList<>(supplier.getProducts()));
                }
                LogUtil.debug("Found supplier by code: " + supplierCode);
            } else {
                LogUtil.debug("Supplier not found with code: " + supplierCode);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by code: " + supplierCode, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Supplier> findSuppliersByName(String name) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.name LIKE :name");
            query.setParameter("name", "%" + name + "%");
            List<Supplier> suppliers = query.list();
            
            // Detach all suppliers and fix RMI serialization
            for (Supplier supplier : suppliers) {
                session.evict(supplier);
                if (supplier.getProducts() != null) {
                    supplier.setProducts(new ArrayList<>(supplier.getProducts()));
                }
            }
            
            LogUtil.debug("Found " + suppliers.size() + " suppliers matching name: " + name);
            return suppliers;
        } catch (Exception e) {
            LogUtil.error("Error finding suppliers by name: " + name, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Supplier findSupplierByEmail(String email) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.email = :email");
            query.setParameter("email", email);
            Supplier supplier = (Supplier) query.uniqueResult();
            
            if (supplier != null) {
                session.evict(supplier);
                // Fix RMI serialization
                if (supplier.getProducts() != null) {
                    supplier.setProducts(new ArrayList<>(supplier.getProducts()));
                }
                LogUtil.debug("Found supplier by email: " + email);
            } else {
                LogUtil.debug("Supplier not found with email: " + email);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier by email: " + email, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Supplier> findAllSuppliers() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Supplier ORDER BY name");
            List<Supplier> suppliers = query.list();
            
            // Detach all suppliers to avoid proxy issues and fix RMI serialization
            for (Supplier supplier : suppliers) {
                session.evict(supplier);
                if (supplier.getProducts() != null) {
                    supplier.setProducts(new ArrayList<>(supplier.getProducts()));
                }
            }
            
            LogUtil.debug("Found " + suppliers.size() + " suppliers in total");
            return suppliers;
        } catch (Exception e) {
            LogUtil.error("Error finding all suppliers", e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Supplier getSupplierWithProducts(int supplierId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s LEFT JOIN FETCH s.products WHERE s.id = :id");
            query.setParameter("id", supplierId);
            Supplier supplier = (Supplier) query.uniqueResult();
            
            if (supplier != null) {
                // Force initialization and detach
                supplier.getProducts().size();
                session.evict(supplier);
                // Also evict all products to prevent proxy issues
                if (supplier.getProducts() != null) {
                    for (Object product : supplier.getProducts()) {
                        session.evict(product);
                    }
                    // Fix RMI serialization - convert to ArrayList
                    supplier.setProducts(new ArrayList<>(supplier.getProducts()));
                }
                LogUtil.debug("Found supplier with products: " + supplierId + 
                             ", Products count: " + supplier.getProducts().size());
            } else {
                LogUtil.debug("Supplier not found with ID: " + supplierId);
            }
            return supplier;
        } catch (Exception e) {
            LogUtil.error("Error finding supplier with products: " + supplierId, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public boolean supplierCodeExists(String supplierCode) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(s) FROM Supplier s WHERE s.supplierCode = :supplierCode");
            query.setParameter("supplierCode", supplierCode);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if supplier code exists: " + supplierCode, e);
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
                "SELECT COUNT(s) FROM Supplier s WHERE s.email = :email");
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
    
    public List<Supplier> findSuppliersByContactPerson(String contactPerson) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Supplier s WHERE s.contactPerson LIKE :contactPerson");
            query.setParameter("contactPerson", "%" + contactPerson + "%");
            List<Supplier> suppliers = query.list();
            
            // Fix RMI serialization for all suppliers
            for (Supplier supplier : suppliers) {
                session.evict(supplier);
                if (supplier.getProducts() != null) {
                    supplier.setProducts(new ArrayList<>(supplier.getProducts()));
                }
            }
            
            LogUtil.debug("Found " + suppliers.size() + " suppliers with contact person: " + contactPerson);
            return suppliers;
        } catch (Exception e) {
            LogUtil.error("Error finding suppliers by contact person: " + contactPerson, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}