package dao;

import model.Customer;
import model.Order;
import model.OrderItem;
import model.Product;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import util.HibernateUtil;
import util.LogUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for Order operations using Hibernate.
 */
public class OrderDao {
    
    /**
     * Creates a new order in the database
     * 
     * @param order The order to create
     * @return The created order with generated ID, or null if failed
     */
    public Order createOrder(Order order) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Save the order first
            session.save(order);
            
            // Save order items and update product stock
            for (OrderItem item : order.getOrderItems()) {
                item.setOrder(order);
                session.save(item);
                
                // Update product stock
                Product product = (Product) session.get(Product.class, item.getProduct().getId());
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    session.update(product);
                }
            }
            
            transaction.commit();
            LogUtil.info("Order created successfully: " + order.getOrderId());
            return order;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to create order: " + order.getOrderId(), e);
            return null;
        }
    }
    
    /**
     * Updates an existing order in the database
     * 
     * @param order The order to update
     * @return The updated order, or null if failed
     */
    public Order updateOrder(Order order) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(order);
            transaction.commit();
            LogUtil.info("Order updated successfully: " + order.getOrderId());
            return order;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update order: " + order.getOrderId(), e);
            return null;
        }
    }
    
    /**
     * Updates the status of an order
     * 
     * @param orderId The ID of the order
     * @param status The new status
     * @return Number of rows affected
     */
    public int updateOrderStatus(int orderId, String status) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            Query query = session.createQuery(
                "UPDATE Order o SET o.status = :status WHERE o.id = :id");
            query.setParameter("status", status);
            query.setParameter("id", orderId);
            int rowsAffected = query.executeUpdate();
            transaction.commit();
            LogUtil.info("Updated order status for order ID " + orderId + " to " + status);
            return rowsAffected;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to update order status for order ID: " + orderId, e);
            return 0;
        }
    }
    
    /**
     * Finds an order by ID
     * 
     * @param id The order ID to search for
     * @return The order if found, null otherwise
     */
    public Order findOrderById(int id) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Order order = (Order) session.get(Order.class, id);
            if (order != null) {
                LogUtil.debug("Found order by ID: " + id);
            } else {
                LogUtil.debug("Order not found with ID: " + id);
            }
            return order;
        } catch (Exception e) {
            LogUtil.error("Error finding order by ID: " + id, e);
            return null;
        }
    }
    
    /**
     * Finds an order by order ID
     * 
     * @param orderId The order ID to search for
     * @return The order if found, null otherwise
     */
    public Order findOrderByOrderId(String orderId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Order o WHERE o.orderId = :orderId");
            query.setParameter("orderId", orderId);
            Order order = (Order) query.uniqueResult();
            
            if (order != null) {
                LogUtil.debug("Found order by order ID: " + orderId);
            } else {
                LogUtil.debug("Order not found with order ID: " + orderId);
            }
            return order;
        } catch (Exception e) {
            LogUtil.error("Error finding order by order ID: " + orderId, e);
            return null;
        }
    }
    
    /**
     * Finds orders by customer
     * 
     * @param customer The customer to search for
     * @return List of matching orders
     */
    public List<Order> findOrdersByCustomer(Customer customer) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Order o WHERE o.customer = :customer ORDER BY o.orderDate DESC");
            query.setParameter("customer", customer);
            List<Order> orders = query.list();
            LogUtil.debug("Found " + orders.size() + " orders for customer: " + customer.getFullName());
            return orders;
        } catch (Exception e) {
            LogUtil.error("Error finding orders by customer: " + customer.getId(), e);
            return null;
        }
    }
    
    /**
     * Finds orders by status
     * 
     * @param status The status to search for
     * @return List of matching orders
     */
    public List<Order> findOrdersByStatus(String status) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC");
            query.setParameter("status", status);
            List<Order> orders = query.list();
            LogUtil.debug("Found " + orders.size() + " orders with status: " + status);
            return orders;
        } catch (Exception e) {
            LogUtil.error("Error finding orders by status: " + status, e);
            return null;
        }
    }
    
    /**
     * Finds orders by date range
     * 
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of matching orders
     */
    public List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC");
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Order> orders = query.list();
            LogUtil.debug("Found " + orders.size() + " orders between " + startDate + " and " + endDate);
            return orders;
        } catch (Exception e) {
            LogUtil.error("Error finding orders by date range: " + startDate + " to " + endDate, e);
            return null;
        }
    }
    
    /**
     * Gets all orders
     * 
     * @return List of all orders
     */
    public List<Order> findAllOrders() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Order ORDER BY orderDate DESC");
            List<Order> orders = query.list();
            LogUtil.debug("Found " + orders.size() + " orders in total");
            return orders;
        } catch (Exception e) {
            LogUtil.error("Error finding all orders", e);
            return null;
        }
    }
    
    /**
     * Gets an order with all its items and customer information
     * 
     * @param orderId The ID of the order
     * @return The order with items and customer loaded
     */
    public Order getOrderWithDetails(int orderId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Order o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.id = :id");
            query.setParameter("id", orderId);
            Order order = (Order) query.uniqueResult();
            
            if (order != null) {
                LogUtil.debug("Found order with details: " + orderId + ", Items count: " + order.getOrderItems().size());
            } else {
                LogUtil.debug("Order not found with ID: " + orderId);
            }
            return order;
        } catch (Exception e) {
            LogUtil.error("Error finding order with details: " + orderId, e);
            return null;
        }
    }
    
    /**
     * Deletes an order from the database
     * 
     * @param order The order to delete
     * @return The deleted order, or null if failed
     */
    public Order deleteOrder(Order order) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            // Restore product stock for order items
            for (OrderItem item : order.getOrderItems()) {
                Product product = (Product) session.get(Product.class, item.getProduct().getId());
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    session.update(product);
                }
            }
            
            // Delete the order (cascade will handle order items)
            session.delete(order);
            transaction.commit();
            LogUtil.info("Order deleted successfully: " + order.getOrderId());
            return order;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LogUtil.error("Failed to delete order: " + order.getOrderId(), e);
            return null;
        }
    }
    
    /**
     * Checks if an order ID already exists
     * 
     * @param orderId The order ID to check
     * @return true if exists, false otherwise
     */
    public boolean orderIdExists(String orderId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(o) FROM Order o WHERE o.orderId = :orderId");
            query.setParameter("orderId", orderId);
            Long count = (Long) query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            LogUtil.error("Error checking if order ID exists: " + orderId, e);
            return false;
        }
    }
}