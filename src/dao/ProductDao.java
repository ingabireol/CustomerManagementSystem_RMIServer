package dao;

import model.Product;
import model.Supplier;
import org.hibernate.Session;
import org.hibernate.Transaction;
//import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.util.List;
import org.hibernate.Query;

/**
 * Data Access Object for Product operations using Hibernate.
 */
public class ProductDao {
    
    /**
     * Creates a new product in the database
     * 
     * @param product The product to create
     * @return The created product with generated ID, or null if failed
     */
    public Product createProduct(Product product) {
        Transaction transaction = null;
        try  {
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
    
    /**
     * Updates an existing product in the database
     * 
     * @param product The product to update
     * @return The updated product, or null if failed
     */
    public Product updateProduct(Product product) {
        Transaction transaction = null;
        try  {
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
    
    /**
     * Updates the stock quantity of a product
     * 
     * @param productId The ID of the product
     * @param quantity The quantity to add (negative to remove)
     * @return Number of rows affected
     */
    public int updateProductStock(int productId, int quantity) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
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
        }
    }
    
    /**
     * Finds a product by ID
     * 
     * @param id The product ID to search for
     * @return The product if found, null otherwise
     */
    public Product findProductById(int id) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Product product = (Product) session.get(Product.class, id);
            if (product != null) {
                LogUtil.debug("Found product by ID: " + id);
            } else {
                LogUtil.debug("Product not found with ID: " + id);
            }
            return product;
        } catch (Exception e) {
            LogUtil.error("Error finding product by ID: " + id, e);
            return null;
        }
    }
    
    /**
     * Finds a product by product code
     * 
     * @param productCode The product code to search for
     * @return The product if found, null otherwise
     */
    public Product findProductByCode(String productCode) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.productCode = :productCode");
            query.setParameter("productCode", productCode);
            Product product = (Product) query.uniqueResult();
            
            if (product != null) {
                LogUtil.debug("Found product by code: " + productCode);
            } else {
                LogUtil.debug("Product not found with code: " + productCode);
            }
            return product;
        } catch (Exception e) {
            LogUtil.error("Error finding product by code: " + productCode, e);
            return null;
        }
    }
    
    /**
     * Finds products by name
     * 
     * @param name The name to search for
     * @return List of matching products
     */
    public List<Product> findProductsByName(String name) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.name LIKE :name");
            query.setParameter("name", "%" + name + "%");
            List<Product> products = query.list();
            LogUtil.debug("Found " + products.size() + " products matching name: " + name);
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding products by name: " + name, e);
            return null;
        }
    }
    
    /**
     * Finds products by category
     * 
     * @param category The category to search for
     * @return List of matching products
     */
    public List<Product> findProductsByCategory(String category) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.category = :category");
            query.setParameter("category", category);
            List<Product> products = query.list();
            LogUtil.debug("Found " + products.size() + " products in category: " + category);
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding products by category: " + category, e);
            return null;
        }
    }
    
    /**
     * Finds products by supplier
     * 
     * @param supplier The supplier to search for
     * @return List of matching products
     */
    public List<Product> findProductsBySupplier(Supplier supplier) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.supplier = :supplier");
            query.setParameter("supplier", supplier);
            List<Product> products = query.list();
            LogUtil.debug("Found " + products.size() + " products for supplier: " + supplier.getName());
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding products by supplier: " + supplier.getId(), e);
            return null;
        }
    }
    
    /**
     * Finds products with low stock below a threshold
     * 
     * @param threshold The stock threshold
     * @return List of products with stock below threshold
     */
    public List<Product> findLowStockProducts(int threshold) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p WHERE p.stockQuantity < :threshold ORDER BY p.stockQuantity");
            query.setParameter("threshold", threshold);
            List<Product> products = query.list();
            LogUtil.debug("Found " + products.size() + " products with low stock (below " + threshold + ")");
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding low stock products with threshold: " + threshold, e);
            return null;
        }
    }
    
    /**
     * Gets all products
     * 
     * @return List of all products
     */
    public List<Product> findAllProducts() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Product ORDER BY name");
            List<Product> products = query.list();
            LogUtil.debug("Found " + products.size() + " products in total");
            return products;
        } catch (Exception e) {
            LogUtil.error("Error finding all products", e);
            return null;
        }
    }
    
    /**
     * Gets a product with its supplier information
     * 
     * @param productId The ID of the product
     * @return The product with supplier loaded
     */
    public Product getProductWithSupplier(int productId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Product p LEFT JOIN FETCH p.supplier WHERE p.id = :id");
            query.setParameter("id", productId);
            Product product = (Product) query.uniqueResult();
            
            if (product != null) {
                LogUtil.debug("Found product with supplier: " + productId);
            } else {
                LogUtil.debug("Product not found with ID: " + productId);
            }
            return product;
        } catch (Exception e) {
            LogUtil.error("Error finding product with supplier: " + productId, e);
            return null;
        }
    }
    
    /**
     * Deletes a product from the database
     * 
     * @param product The product to delete
     * @return The deleted product, or null if failed
     */
    public Product deleteProduct(Product product) {
        Transaction transaction = null;
        try  {
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
    
    /**
     * Checks if a product code already exists
     * 
     * @param productCode The product code to check
     * @return true if exists, false otherwise
     */
    public boolean productCodeExists(String productCode) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.productCode = :productCode");
            query.setParameter("productCode", productCode);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if product code exists: " + productCode, e);
            return false;
        }
    }
    
    /**
     * Gets all distinct categories
     * 
     * @return List of all categories
     */
    public List<String> findAllCategories() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category");
            List<String> categories = query.list();
            LogUtil.debug("Found " + categories.size() + " distinct categories");
            return categories;
        } catch (Exception e) {
            LogUtil.error("Error finding all categories", e);
            return null;
        }
    }
}