package dao;

import java.util.ArrayList;
import model.Product;
import model.Supplier;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

/**
 * FIXED: ProductDao with proper RMI serialization handling
 */
public class ProductDao {
    
    public Product createProduct(Product product) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(product);
            transaction.commit();
            LogUtil.info("Product created successfully: " + product.getProductCode());
            return product;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create product: " + product.getProductCode(), e);
            return null;
        }
    }
    
    public Product updateProduct(Product product) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(product);
            transaction.commit();
            LogUtil.info("Product updated successfully: " + product.getProductCode());
            return product;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update product: " + product.getProductCode(), e);
            return null;
        }
    }
    
    public int updateProductStock(int productId, int quantity) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            Query query = session.createQuery(
                "UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id");
            query.setParameter("quantity", quantity);
            query.setParameter("id", productId);
            int rowsAffected = query.executeUpdate();
            transaction.commit();
            LogUtil.info("Updated stock for product ID " + productId + " by " + quantity + " units");
            return rowsAffected;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update stock for product ID: " + productId, e);
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Product findProductById(int id) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Product product = (Product) session.get(Product.class, id);
            if (product != null) {
                session.evict(product); // Detach for RMI
                LogUtil.debug("Found product by ID: " + id);
            } else {
                LogUtil.debug("Product not found with ID: " + id);
            }
            return product;
        } catch (Exception e) {
            LogUtil.error("Error finding product by ID: " + id, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Product findProductByCode(String productCode) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.productCode = :productCode");
            query.setParameter("productCode", productCode);
            Product product = (Product) query.uniqueResult();
            
            if (product != null) {
                session.evict(product);
                LogUtil.debug("Found product by code: " + productCode);
            } else {
                LogUtil.debug("Product not found with code: " + productCode);
            }
            return product;
        } catch (Exception e) {
            LogUtil.error("Error finding product by code: " + productCode, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Product> findProductsByName(String name) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.name LIKE :name");
            query.setParameter("name", "%" + name + "%");
            List<Product> products = query.list();
            
            // Detach all products
            for (Product product : products) {
                session.evict(product);
            }
            
            LogUtil.debug("Found " + products.size() + " products matching name: " + name);
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding products by name: " + name, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Product> findProductsByCategory(String category) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.category = :category");
            query.setParameter("category", category);
            List<Product> products = query.list();
            
            for (Product product : products) {
                session.evict(product);
            }
            
            LogUtil.debug("Found " + products.size() + " products in category: " + category);
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding products by category: " + category, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Product> findProductsBySupplier(Supplier supplier) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.supplier.id = :supplierId");
            query.setParameter("supplierId", supplier.getId());
            List<Product> products = query.list();
            
            for (Product product : products) {
                session.evict(product);
            }
            
            LogUtil.debug("Found " + products.size() + " products for supplier: " + supplier.getName());
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding products by supplier: " + supplier.getId(), e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Product> findLowStockProducts(int threshold) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.stockQuantity < :threshold ORDER BY p.stockQuantity");
            query.setParameter("threshold", threshold);
            List<Product> products = query.list();
            
            for (Product product : products) {
                session.evict(product);
            }
            
            LogUtil.debug("Found " + products.size() + " products with low stock (below " + threshold + ")");
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding low stock products with threshold: " + threshold, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Product> findAllProducts() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Product ORDER BY name");
            List<Product> products = query.list();
            
            for (Product product : products) {
                product.setSupplier(unproxy(product.getSupplier()));
                session.evict(product);
            }
            
            LogUtil.debug("Found " + products.size() + " products in total");
            return new ArrayList<>(products);
        } catch (Exception e) {
            LogUtil.error("Error finding all products", e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public static <T> T unproxy(T entity) {
    if (entity instanceof HibernateProxy) {
        return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
    }
    return entity;
    }
    
    public Product getProductWithSupplier(int productId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p LEFT JOIN FETCH p.supplier WHERE p.id = :id");
            query.setParameter("id", productId);
            Product product = (Product) query.uniqueResult();
            
            if (product != null) {
                session.evict(product);
                if (product.getSupplier() != null) {
                    session.evict(product.getSupplier());
                    product.setSupplier(unproxy(product.getSupplier()));
                }
                LogUtil.debug("Found product with supplier: " + productId);
            } else {
                LogUtil.debug("Product not found with ID: " + productId);
            }
            return product;
        } catch (Exception e) {
            LogUtil.error("Error finding product with supplier: " + productId, e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Product deleteProduct(Product product) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.delete(product);
            transaction.commit();
            LogUtil.info("Product deleted successfully: " + product.getProductCode());
            return product;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete product: " + product.getProductCode(), e);
            return null;
        }
    }
    
    public boolean productCodeExists(String productCode) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.productCode = :productCode");
            query.setParameter("productCode", productCode);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if product code exists: " + productCode, e);
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<String> findAllCategories() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category");
            List<String> categories = query.list();
            LogUtil.debug("Found " + categories.size() + " distinct categories");
            return categories;
        } catch (Exception e) {
            LogUtil.error("Error finding all categories", e);
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}