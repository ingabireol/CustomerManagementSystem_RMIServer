package service.implementation;

import dao.OrderDao;
import model.Customer;
import model.Order;
import service.OrderService;
import util.LogUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of OrderService interface.
 * Handles business logic for order operations.
 */
public class OrderServiceImpl extends UnicastRemoteObject implements OrderService {
    
    private OrderDao orderDao;
    
    /**
     * Constructor
     * 
     * @throws RemoteException If RMI initialization fails
     */
    public OrderServiceImpl() throws RemoteException {
        super();
        this.orderDao = new OrderDao();
        LogUtil.info("OrderService initialized");
    }
    
    @Override
    public Order createOrder(Order order) throws RemoteException {
        try {
            // Validate input
            if (order == null) {
                LogUtil.warn("Attempted to create null order");
                return null;
            }
            
            if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
                LogUtil.warn("Attempted to create order without order ID");
                return null;
            }
            
            if (order.getCustomer() == null) {
                LogUtil.warn("Attempted to create order without customer");
                return null;
            }
            
            if (orderDao.orderIdExists(order.getOrderId())) {
                LogUtil.warn("Attempted to create order with existing order ID: " + order.getOrderId());
                return null;
            }
            
            return orderDao.createOrder(order);
        } catch (Exception e) {
            LogUtil.error("Error creating order", e);
            throw new RemoteException("Failed to create order", e);
        }
    }
    
    @Override
    public Order updateOrder(Order order) throws RemoteException {
        try {
            if (order == null || order.getId() <= 0) {
                LogUtil.warn("Attempted to update invalid order");
                return null;
            }
            
            return orderDao.updateOrder(order);
        } catch (Exception e) {
            LogUtil.error("Error updating order", e);
            throw new RemoteException("Failed to update order", e);
        }
    }
    
    @Override
    public int updateOrderStatus(int orderId, String status) throws RemoteException {
        try {
            if (orderId <= 0) {
                LogUtil.warn("Invalid order ID provided for status update: " + orderId);
                return 0;
            }
            
            if (status == null || status.trim().isEmpty()) {
                LogUtil.warn("Invalid status provided for order update");
                return 0;
            }
            
            return orderDao.updateOrderStatus(orderId, status.trim());
        } catch (Exception e) {
            LogUtil.error("Error updating order status for ID: " + orderId, e);
            throw new RemoteException("Failed to update order status", e);
        }
    }
    
    @Override
    public Order deleteOrder(Order order) throws RemoteException {
        try {
            if (order == null || order.getId() <= 0) {
                LogUtil.warn("Attempted to delete invalid order");
                return null;
            }
            
            return orderDao.deleteOrder(order);
        } catch (Exception e) {
            LogUtil.error("Error deleting order", e);
            throw new RemoteException("Failed to delete order", e);
        }
    }
    
    @Override
    public Order findOrderById(int id) throws RemoteException {
        try {
            if (id <= 0) {
                LogUtil.warn("Invalid order ID provided: " + id);
                return null;
            }
            
            return orderDao.findOrderById(id);
        } catch (Exception e) {
            LogUtil.error("Error finding order by ID: " + id, e);
            throw new RemoteException("Failed to find order by ID", e);
        }
    }
    
    @Override
    public Order findOrderByOrderId(String orderId) throws RemoteException {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                LogUtil.warn("Invalid order ID provided");
                return null;
            }
            
            return orderDao.findOrderByOrderId(orderId.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding order by order ID: " + orderId, e);
            throw new RemoteException("Failed to find order by order ID", e);
        }
    }
    
    @Override
    public List<Order> findOrdersByCustomer(Customer customer) throws RemoteException {
        try {
            if (customer == null || customer.getId() <= 0) {
                LogUtil.warn("Invalid customer provided for order search");
                return null;
            }
            
            return orderDao.findOrdersByCustomer(customer);
        } catch (Exception e) {
            LogUtil.error("Error finding orders by customer: " + customer.getId(), e);
            throw new RemoteException("Failed to find orders by customer", e);
        }
    }
    
    @Override
    public List<Order> findOrdersByStatus(String status) throws RemoteException {
        try {
            if (status == null || status.trim().isEmpty()) {
                LogUtil.warn("Invalid status provided for order search");
                return null;
            }
            
            return orderDao.findOrdersByStatus(status.trim());
        } catch (Exception e) {
            LogUtil.error("Error finding orders by status: " + status, e);
            throw new RemoteException("Failed to find orders by status", e);
        }
    }
    
    @Override
    public List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException {
        try {
            if (startDate == null || endDate == null) {
                LogUtil.warn("Invalid date range provided for order search");
                return null;
            }
            
            if (startDate.isAfter(endDate)) {
                LogUtil.warn("Start date is after end date");
                return null;
            }
            
            return orderDao.findOrdersByDateRange(startDate, endDate);
        } catch (Exception e) {
            LogUtil.error("Error finding orders by date range: " + startDate + " to " + endDate, e);
            throw new RemoteException("Failed to find orders by date range", e);
        }
    }
    
    @Override
    public List<Order> findAllOrders() throws RemoteException {
        try {
            return orderDao.findAllOrders();
        } catch (Exception e) {
            LogUtil.error("Error finding all orders", e);
            throw new RemoteException("Failed to find all orders", e);
        }
    }
    
    @Override
    public Order getOrderWithDetails(int orderId) throws RemoteException {
        try {
            if (orderId <= 0) {
                LogUtil.warn("Invalid order ID provided: " + orderId);
                return null;
            }
            
            return orderDao.getOrderWithDetails(orderId);
        } catch (Exception e) {
            LogUtil.error("Error getting order with details: " + orderId, e);
            throw new RemoteException("Failed to get order with details", e);
        }
    }
    
    @Override
    public boolean orderIdExists(String orderId) throws RemoteException {
        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                return false;
            }
            
            return orderDao.orderIdExists(orderId.trim());
        } catch (Exception e) {
            LogUtil.error("Error checking if order ID exists: " + orderId, e);
            throw new RemoteException("Failed to check order ID existence", e);
        }
    }
}