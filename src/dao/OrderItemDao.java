package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.OrderItem;
import model.Product;

/**
 * Data Access Object for OrderItem operations.
 */
public class OrderItemDao {
    private String db_url = "jdbc:postgresql://localhost:5432/business_db";
    private String db_username = "postgres";
    private String db_passwd = "078868";
    
    /**
     * Creates a new order item in the database
     * 
     * @param orderItem The order item to create
     * @return Number of rows affected
     */
    public int createOrderItem(OrderItem orderItem) {
        try {
            // Create connection
            Connection con = DriverManager.getConnection(db_url, db_username, db_passwd);
            
            // Prepare statement
            String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) " +
                         "VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pst.setInt(1, orderItem.getOrderId());
            pst.setInt(2, orderItem.getProductId());
            pst.setInt(3, orderItem.getQuantity());
            pst.setBigDecimal(4, orderItem.getUnitPrice());
            
            // Execute statement
            int rowsAffected = pst.executeUpdate();
            
            // Get generated ID
            if (rowsAffected > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    orderItem.setId(rs.getInt(1));
                }
                rs.close();
            }
            
            // Update product stock
            sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
            PreparedStatement stockPst = con.prepareStatement(sql);
            stockPst.setInt(1, orderItem.getQuantity());
            stockPst.setInt(2, orderItem.getProductId());
            stockPst.executeUpdate();
            stockPst.close();
            
            // Close connection
            con.close();
            return rowsAffected;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Updates an existing order item in the database
     * 
     * @param orderItem The order item to update
     * @param oldQuantity The previous quantity for inventory adjustment
     * @return Number of rows affected
     */
    public int updateOrderItem(OrderItem orderItem, int oldQuantity) {
        Connection con = null;
        try {
            // Create connection
            con = DriverManager.getConnection(db_url, db_username, db_passwd);
            
            // Start transaction
            con.setAutoCommit(false);
            
            // Prepare statement
            String sql = "UPDATE order_items SET order_id = ?, product_id = ?, quantity = ?, unit_price = ? WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            
            pst.setInt(1, orderItem.getOrderId());
            pst.setInt(2, orderItem.getProductId());
            pst.setInt(3, orderItem.getQuantity());
            pst.setBigDecimal(4, orderItem.getUnitPrice());
            pst.setInt(5, orderItem.getId());
            
            // Execute statement
            int rowsAffected = pst.executeUpdate();
            
            // Update product stock - adjust for difference between old and new quantity
            int quantityDifference = orderItem.getQuantity() - oldQuantity;
            if (quantityDifference != 0) {
                sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
                PreparedStatement stockPst = con.prepareStatement(sql);
                stockPst.setInt(1, quantityDifference);
                stockPst.setInt(2, orderItem.getProductId());
                stockPst.executeUpdate();
                stockPst.close();
            }
            
            // Commit transaction
            con.commit();
            
            // Close connection
            con.close();
            return rowsAffected;
            
        } catch (Exception ex) {
            try {
                // Rollback transaction on error
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            
            ex.printStackTrace();
            return 0;
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception closeEx) {
                closeEx.printStackTrace();
            }
        }
    }
    
    /**
     * Finds an order item by ID
     * 
     * @param id The order item ID to search for
     * @return The order item if found, null otherwise
     */
    public OrderItem findOrderItemById(int id) {
        try {
            Connection con = DriverManager.getConnection(db_url, db_username, db_passwd);
            String sql = "SELECT * FROM order_items WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, id);
            
            ResultSet rs = pst.executeQuery();
            OrderItem orderItem = null;
            
            if (rs.next()) {
                orderItem = new OrderItem();
                orderItem.setId(rs.getInt("id"));
                orderItem.setOrderId(rs.getInt("order_id"));
                orderItem.setProductId(rs.getInt("product_id"));
                orderItem.setQuantity(rs.getInt("quantity"));
                orderItem.setUnitPrice(rs.getBigDecimal("unit_price"));
            }
            
            con.close();
            return orderItem;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Finds order items by order ID
     * 
     * @param orderId The order ID to search for
     * @return List of matching order items
     */
    public List<OrderItem> findOrderItemsByOrderId(int orderId) {
        try {
            Connection con = DriverManager.getConnection(db_url, db_username, db_passwd);
            String sql = "SELECT oi.*, p.name as product_name FROM order_items oi " +
                         "JOIN products p ON oi.product_id = p.id " +
                         "WHERE oi.order_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, orderId);
            
            ResultSet rs = pst.executeQuery();
            List<OrderItem> orderItemList = new ArrayList<>();
            
            while (rs.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(rs.getInt("id"));
                orderItem.setOrderId(rs.getInt("order_id"));
                orderItem.setProductId(rs.getInt("product_id"));
                orderItem.setQuantity(rs.getInt("quantity"));
                orderItem.setUnitPrice(rs.getBigDecimal("unit_price"));
                
                // Create a simple product with basic info
                Product product = new Product();
                product.setId(orderItem.getProductId());
                product.setName(rs.getString("product_name"));
                orderItem.setProduct(product);
                
                orderItemList.add(orderItem);
            }
            
            con.close();
            return orderItemList;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Finds order items by product ID
     * 
     * @param productId The product ID to search for
     * @return List of matching order items
     */
    public List<OrderItem> findOrderItemsByProductId(int productId) {
        try {
            Connection con = DriverManager.getConnection(db_url, db_username, db_passwd);
            String sql = "SELECT * FROM order_items WHERE product_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, productId);
            
            ResultSet rs = pst.executeQuery();
            List<OrderItem> orderItemList = new ArrayList<>();
            
            while (rs.next()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(rs.getInt("id"));
                orderItem.setOrderId(rs.getInt("order_id"));
                orderItem.setProductId(rs.getInt("product_id"));
                orderItem.setQuantity(rs.getInt("quantity"));
                orderItem.setUnitPrice(rs.getBigDecimal("unit_price"));
                orderItemList.add(orderItem);
            }
            
            con.close();
            return orderItemList;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Gets an order item with product details
     * 
     * @param orderItemId The ID of the order item
     * @return The order item with product loaded
     */
    public OrderItem getOrderItemWithProduct(int orderItemId) {
        try {
            Connection con = DriverManager.getConnection(db_url, db_username, db_passwd);
            String sql = "SELECT oi.*, p.product_code, p.name, p.description, p.price, p.category " +
                         "FROM order_items oi " +
                         "JOIN products p ON oi.product_id = p.id " +
                         "WHERE oi.id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, orderItemId);
            
            ResultSet rs = pst.executeQuery();
            OrderItem orderItem = null;
            
            if (rs.next()) {
                orderItem = new OrderItem();
                orderItem.setId(rs.getInt("id"));
                orderItem.setOrderId(rs.getInt("order_id"));
                orderItem.setProductId(rs.getInt("product_id"));
                orderItem.setQuantity(rs.getInt("quantity"));
                orderItem.setUnitPrice(rs.getBigDecimal("unit_price"));
                
                Product product = new Product();
                product.setId(orderItem.getProductId());
                product.setProductCode(rs.getString("product_code"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setCategory(rs.getString("category"));
                
                orderItem.setProduct(product);
            }
            
            con.close();
            return orderItem;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Deletes an order item from the database
     * 
     * @param orderItemId The ID of the order item to delete
     * @return Number of rows affected
     */
    public int deleteOrderItem(int orderItemId) {
        Connection con = null;
        try {
            // First get the order item to know how much stock to return
            OrderItem orderItem = findOrderItemById(orderItemId);
            if (orderItem == null) {
                return 0;
            }
            
            // Create connection
            con = DriverManager.getConnection(db_url, db_username, db_passwd);
            
            // Start transaction
            con.setAutoCommit(false);
            
            // Delete the order item
            String sql = "DELETE FROM order_items WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, orderItemId);
            
            int rowsAffected = pst.executeUpdate();
            
            // Update product stock - add back the quantity
            sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
            PreparedStatement stockPst = con.prepareStatement(sql);
            stockPst.setInt(1, orderItem.getQuantity());
            stockPst.setInt(2, orderItem.getProductId());
            stockPst.executeUpdate();
            
            // Commit transaction
            con.commit();
            
            // Close connection
            con.close();
            return rowsAffected;
            
        } catch (Exception ex) {
            try {
                // Rollback transaction on error
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            
            ex.printStackTrace();
            return 0;
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception closeEx) {
                closeEx.printStackTrace();
            }
        }
    }
}