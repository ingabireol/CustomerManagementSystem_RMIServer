package util;

import model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to help convert Hibernate collections to RMI-safe collections
 * This prevents the ClassNotFoundException for PersistentBag and similar Hibernate collections
 */
public class HibernateCollectionUtil {
    
    /**
     * Converts a Customer object to be RMI-safe by converting Hibernate collections
     * 
     * @param customer The customer to convert
     * @return RMI-safe customer object
     */
    public static Customer makeCustomerRMISafe(Customer customer) {
        if (customer != null && customer.getOrders() != null) {
            customer.setOrders(new ArrayList<>(customer.getOrders()));
        }
        return customer;
    }
    
    /**
     * Converts a list of Customer objects to be RMI-safe
     * 
     * @param customers The list of customers to convert
     * @return List of RMI-safe customer objects
     */
    public static List<Customer> makeCustomersRMISafe(List<Customer> customers) {
        if (customers != null) {
            for (Customer customer : customers) {
                makeCustomerRMISafe(customer);
            }
        }
        return customers;
    }
    
    /**
     * Converts a Supplier object to be RMI-safe by converting Hibernate collections
     * 
     * @param supplier The supplier to convert
     * @return RMI-safe supplier object
     */
    public static Supplier makeSupplierRMISafe(Supplier supplier) {
        if (supplier != null && supplier.getProducts() != null) {
            supplier.setProducts(new ArrayList<>(supplier.getProducts()));
        }
        return supplier;
    }
    
    /**
     * Converts a list of Supplier objects to be RMI-safe
     * 
     * @param suppliers The list of suppliers to convert
     * @return List of RMI-safe supplier objects
     */
    public static List<Supplier> makeSuppliersRMISafe(List<Supplier> suppliers) {
        if (suppliers != null) {
            for (Supplier supplier : suppliers) {
                makeSupplierRMISafe(supplier);
            }
        }
        return suppliers;
    }
    
    /**
     * Converts an Order object to be RMI-safe by converting Hibernate collections
     * 
     * @param order The order to convert
     * @return RMI-safe order object
     */
    public static Order makeOrderRMISafe(Order order) {
        if (order != null) {
            if (order.getOrderItems() != null) {
                order.setOrderItems(new ArrayList<>(order.getOrderItems()));
            }
            if (order.getInvoices() != null) {
                order.setInvoices(new ArrayList<>(order.getInvoices()));
            }
        }
        return order;
    }
    
    /**
     * Converts a list of Order objects to be RMI-safe
     * 
     * @param orders The list of orders to convert
     * @return List of RMI-safe order objects
     */
    public static List<Order> makeOrdersRMISafe(List<Order> orders) {
        if (orders != null) {
            for (Order order : orders) {
                makeOrderRMISafe(order);
            }
        }
        return orders;
    }
    
    /**
     * Converts an Invoice object to be RMI-safe by converting Hibernate collections
     * 
     * @param invoice The invoice to convert
     * @return RMI-safe invoice object
     */
    public static Invoice makeInvoiceRMISafe(Invoice invoice) {
        if (invoice != null && invoice.getPayments() != null) {
            invoice.setPayments(new ArrayList<>(invoice.getPayments()));
        }
        return invoice;
    }
    
    /**
     * Converts a list of Invoice objects to be RMI-safe
     * 
     * @param invoices The list of invoices to convert
     * @return List of RMI-safe invoice objects
     */
    public static List<Invoice> makeInvoicesRMISafe(List<Invoice> invoices) {
        if (invoices != null) {
            for (Invoice invoice : invoices) {
                makeInvoiceRMISafe(invoice);
            }
        }
        return invoices;
    }
    
    /**
     * Converts a Product object to be RMI-safe (no collections, but for consistency)
     * 
     * @param product The product to convert
     * @return RMI-safe product object
     */
    public static Product makeProductRMISafe(Product product) {
        // Product doesn't have collections that cause RMI issues, but included for completeness
        return product;
    }
    
    /**
     * Converts a list of Product objects to be RMI-safe
     * 
     * @param products The list of products to convert
     * @return List of RMI-safe product objects
     */
    public static List<Product> makeProductsRMISafe(List<Product> products) {
        // Products don't have collections that cause RMI issues, but included for completeness
        return products;
    }
    
    /**
     * Converts a Payment object to be RMI-safe (no collections, but for consistency)
     * 
     * @param payment The payment to convert
     * @return RMI-safe payment object
     */
    public static Payment makePaymentRMISafe(Payment payment) {
        // Payment doesn't have collections that cause RMI issues, but included for completeness
        return payment;
    }
    
    /**
     * Converts a list of Payment objects to be RMI-safe
     * 
     * @param payments The list of payments to convert
     * @return List of RMI-safe payment objects
     */
    public static List<Payment> makePaymentsRMISafe(List<Payment> payments) {
        // Payments don't have collections that cause RMI issues, but included for completeness
        return payments;
    }
    
    /**
     * Converts an OrderItem object to be RMI-safe (no collections, but for consistency)
     * 
     * @param orderItem The order item to convert
     * @return RMI-safe order item object
     */
    public static OrderItem makeOrderItemRMISafe(OrderItem orderItem) {
        // OrderItem doesn't have collections that cause RMI issues, but included for completeness
        return orderItem;
    }
    
    /**
     * Converts a list of OrderItem objects to be RMI-safe
     * 
     * @param orderItems The list of order items to convert
     * @return List of RMI-safe order item objects
     */
    public static List<OrderItem> makeOrderItemsRMISafe(List<OrderItem> orderItems) {
        // OrderItems don't have collections that cause RMI issues, but included for completeness
        return orderItems;
    }
    
    /**
     * Converts a User object to be RMI-safe (no collections, but for consistency)
     * 
     * @param user The user to convert
     * @return RMI-safe user object
     */
    public static User makeUserRMISafe(User user) {
        // User doesn't have collections that cause RMI issues, but included for completeness
        return user;
    }
    
    /**
     * Converts a list of User objects to be RMI-safe
     * 
     * @param users The list of users to convert
     * @return List of RMI-safe user objects
     */
    public static List<User> makeUsersRMISafe(List<User> users) {
        // Users don't have collections that cause RMI issues, but included for completeness
        return users;
    }
}