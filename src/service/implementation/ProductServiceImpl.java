package service.implementation;

import dao.ProductDao;
import model.Product;
import model.Supplier;
import service.ProductService;
import util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementation of ProductService interface.
 * Handles business logic for product operations.
 */
public class ProductServiceImpl extends UnicastRemoteObject implements ProductService {
    
    private ProductDao productDao;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public ProductServiceImpl() throws RemoteException {
        super();
        this.productDao = new ProductDao();
        LogUtil.info("ProductService initialized");
    }
    
    @Override
    public Product createProduct(Product product) throws RemoteException {
        try {
            // Validate input
            if (product == null) {
                LogUtil.warn("Attempted to create null product");
                return null;
            }
            
            if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
                LogUtil.warn("Attempted to create product without product code");
                return null;
            }
            
            if (productDao.productCodeExists(product.getProductCode())) {
                LogUtil.warn("Attempted to create product with existing product code: " + product.getProductCode());
                return null;
            }
            
            return productDao.createProduct(product);
        } catch (Exception e) {
            LogUtil.error("Error creating product", e);
            throw new RemoteException("Failed to create product", e);
        }
    }
    
    @Override
    public Product updateProduct(Product product) throws RemoteException {
        try {
            if (product == null || product.getId() <= 0) {
                LogUtil.warn("Attempted to update invalid product");
                return null;
            }
            
            return productDao.updateProduct(product);
        } catch (Exception e) {
            LogUtil.error("Error updating product", e);
            throw new RemoteException("Failed to update product", e);
        }
    }
    
    @Override
    public int updateProductStock(int productId, int quantity) throws RemoteException {
        try {
            if (productId <= 0) {
                LogUtil.warn("Invalid product ID provided for stock update: " + productId);
                return 0;
            }
            
            return productDao.updateProductStock(productId, quantity);
        } catch (Exception e) {
            LogUtil.error("Error updating product stock for ID: " + productId, e);
            throw new RemoteException("Failed to update product stock", e);
        }
    }
    
    @Override
    public Product deleteProduct(Product product) throws RemoteException {
        try {
            if (product == null || product.getId() <= 0) {
                LogUtil.warn("Attempted to delete invalid product");
                return null;
            }
            
            return productDao.deleteProduct(product);
        } catch (Exception e) {
            LogUtil.error("Error deleting product", e);
            throw new RemoteException("Failed to delete product", e);
        }
    }
    
    @Override
    public Product findProductById(int id) throws RemoteException {
        try {
            if (id <= 0) {
                LogUtil.warn("Invalid product ID provided: " + id);
                return null;
            }
            
            return productDao.findProductById(id);
        } catch (Exception e) {
            LogUtil.error("Error finding product by ID: " + id, e);
            throw new RemoteException("Failed to find product by ID", e);
        }
    }
    
    @Override
    public Product findProductByCode(String productCode) throws RemoteException {
        try {
            if (productCode == null || productCode.trim().isEmpty()) {
                LogUtil.warn("Invalid product code provided");
                return null;
            }
            
            return productDao.findProductByCode(productCode.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding product by code: " + productCode, e);
            throw new RemoteException("Failed to find product by code", e);
        }
    }
    
    @Override
    public List<Product> findProductsByName(String name) throws RemoteException {
        try {
            if (name == null || name.trim().isEmpty()) {
                LogUtil.warn("Invalid name provided for product search");
                return null;
            }
            
            return productDao.findProductsByName(name.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding products by name: " + name, e);
            throw new RemoteException("Failed to find products by name", e);
        }
    }
    
    @Override
    public List<Product> findProductsByCategory(String category) throws RemoteException {
        try {
            if (category == null || category.trim().isEmpty()) {
                LogUtil.warn("Invalid category provided for product search");
                return null;
            }
            
            return productDao.findProductsByCategory(category.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding products by category: " + category, e);
            throw new RemoteException("Failed to find products by category", e);
        }
    }
    
    @Override
    public List<Product> findProductsBySupplier(Supplier supplier) throws RemoteException {
        try {
            if (supplier == null || supplier.getId() <= 0) {
                LogUtil.warn("Invalid supplier provided for product search");
                return null;
            }
            
            return productDao.findProductsBySupplier(supplier);
        } catch (Exception e) {
            LogUtil.error("Error finding products by supplier: " + supplier.getId(), e);
            throw new RemoteException("Failed to find products by supplier", e);
        }
    }
    
    @Override
    public List<Product> findLowStockProducts(int threshold) throws RemoteException {
        try {
            if (threshold < 0) {
                LogUtil.warn("Invalid threshold provided: " + threshold);
                return null;
            }
            
            return productDao.findLowStockProducts(threshold);
        } catch (Exception e) {
            LogUtil.error("Error finding low stock products with threshold: " + threshold, e);
            throw new RemoteException("Failed to find low stock products", e);
        }
    }
    
    @Override
    public List<Product> findAllProducts() throws RemoteException {
        try {
            return productDao.findAllProducts();
        } catch (Exception e) {
            LogUtil.error("Error finding all products", e);
            throw new RemoteException("Failed to find all products", e);
        }
    }
    
    @Override
    public Product getProductWithSupplier(int productId) throws RemoteException {
        try {
            if (productId <= 0) {
                LogUtil.warn("Invalid product ID provided: " + productId);
                return null;
            }
            
            return productDao.getProductWithSupplier(productId);
        } catch (Exception e) {
            LogUtil.error("Error getting product with supplier: " + productId, e);
            throw new RemoteException("Failed to get product with supplier", e);
        }
    }
    
    @Override
    public boolean productCodeExists(String productCode) throws RemoteException {
        try {
            if (productCode == null || productCode.trim().isEmpty()) {
                return false;
            }
            
            return productDao.productCodeExists(productCode.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if product code exists: " + productCode, e);
            throw new RemoteException("Failed to check product code existence", e);
        }
    }
    
    @Override
    public List<String> findAllCategories() throws RemoteException {
        try {
            return productDao.findAllCategories();
        } catch (Exception e) {
            LogUtil.error("Error finding all categories", e);
            throw new RemoteException("Failed to find all categories", e);
        }
    }
}