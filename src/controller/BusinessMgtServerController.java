package controller;

import service.implementation.*;
import util.HibernateUtil;
import util.LogUtil;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Business Management Server Controller.
 * Manages the RMI server, services, and provides administrative functions.
 */
public class BusinessMgtServerController {
    
    // Server configuration
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 4444;
    private static final String SERVER_VERSION = "1.0.0";
    
    // Server components
    private Registry registry;
    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;
    
    // Service instances
    private CustomerServiceImpl customerService;
    private ProductServiceImpl productService;
    private SupplierServiceImpl supplierService;
    private OrderServiceImpl orderService;
    private InvoiceServiceImpl invoiceService;
    private PaymentServiceImpl paymentService;
    private UserServiceImpl userService;
    
    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        BusinessMgtServerController server = new BusinessMgtServerController();
        
        // Handle command line arguments
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "--help":
                case "-h":
                    printUsage();
                    return;
                case "--version":
                case "-v":
                    printVersion();
                    return;
                case "--test":
                case "-t":
                    server.testDatabaseConnection();
                    return;
            }
        }
        
        server.startServer();
    }
    
    /**
     * Starts the server with full initialization
     */
    public void startServer() {
        try {
            printBanner();
            LogUtil.info("Starting Business Management Server v" + SERVER_VERSION);
            
            // Initialize components step by step
            initializeDatabase();
            configureRMI();
            createRMIRegistry();
            initializeServices();
            registerServices();
            startMonitoring();
            addShutdownHook();
            
            isRunning = true;
            LogUtil.info("✓ Server started successfully on " + SERVER_HOST + ":" + SERVER_PORT);
            printServerStatus();
            
            // Start interactive console
            startInteractiveConsole();
            
        } catch (Exception e) {
            LogUtil.error("Failed to start Business Management Server", e);
            shutdown();
            System.exit(1);
        }
    }
    
    /**
     * Prints application banner
     */
    private void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              Business Management System Server               ║");
        System.out.println("║                        Version " + SERVER_VERSION + "                         ║");
        System.out.println("║              Hibernate + RMI Implementation                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Initializes and tests database connection
     */
    private void initializeDatabase() throws Exception {
        LogUtil.info("Initializing database connection...");
        try {
            // Test Hibernate SessionFactory
            HibernateUtil.getSessionFactory();
            LogUtil.info("✓ Database connection established successfully");
        } catch (Exception e) {
            LogUtil.error("✗ Database connection failed", e);
            throw new Exception("Cannot start server without database connection", e);
        }
    }
    
    /**
     * Configures RMI system properties
     */
    private void configureRMI() {
        LogUtil.info("Configuring RMI properties...");
        System.setProperty("java.rmi.server.hostname", SERVER_HOST);
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        System.setProperty("java.rmi.dgc.leaseValue", "600000"); // 10 minutes
        LogUtil.info("✓ RMI properties configured");
    }
    
    /**
     * Creates the RMI registry
     */
    private void createRMIRegistry() throws Exception {
        LogUtil.info("Creating RMI registry on port " + SERVER_PORT + "...");
        try {
            registry = LocateRegistry.createRegistry(SERVER_PORT);
            LogUtil.info("✓ RMI Registry created successfully");
        } catch (RemoteException e) {
            LogUtil.error("✗ Failed to create RMI registry", e);
            throw new Exception("RMI registry creation failed", e);
        }
    }
    
    /**
     * Initializes all service implementations
     */
    private void initializeServices() throws Exception {
        LogUtil.info("Initializing business services...");
        try {
            customerService = new CustomerServiceImpl();
            productService = new ProductServiceImpl();
            supplierService = new SupplierServiceImpl();
            orderService = new OrderServiceImpl();
            invoiceService = new InvoiceServiceImpl();
            paymentService = new PaymentServiceImpl();
            userService = new UserServiceImpl();
            LogUtil.info("✓ All services initialized successfully");
        } catch (RemoteException e) {
            LogUtil.error("✗ Failed to initialize services", e);
            throw new Exception("Service initialization failed", e);
        }
    }
    
    /**
     * Registers all services with the RMI registry
     */
    private void registerServices() throws Exception {
        LogUtil.info("Registering services with RMI registry...");
        try {
            registry.rebind("customerService", customerService);
            LogUtil.info("  ✓ Customer Service registered");
            
            registry.rebind("productService", productService);
            LogUtil.info("  ✓ Product Service registered");
            
            registry.rebind("supplierService", supplierService);
            LogUtil.info("  ✓ Supplier Service registered");
            
            registry.rebind("orderService", orderService);
            LogUtil.info("  ✓ Order Service registered");
            
            registry.rebind("invoiceService", invoiceService);
            LogUtil.info("  ✓ Invoice Service registered");
            
            registry.rebind("paymentService", paymentService);
            LogUtil.info("  ✓ Payment Service registered");
            
            registry.rebind("userService", userService);
            LogUtil.info("  ✓ User Service registered");
            
            LogUtil.info("✓ All services registered successfully");
            
            // Create default admin user if needed
            createDefaultAdminIfNeeded();
            
        } catch (RemoteException e) {
            LogUtil.error("✗ Failed to register services", e);
            throw new Exception("Service registration failed", e);
        }
    }
    
    /**
     * Starts server monitoring and health checks
     */
    private void startMonitoring() {
        LogUtil.info("Starting server monitoring...");
        scheduler = Executors.newScheduledThreadPool(2);
        
        // Health check every 5 minutes
        scheduler.scheduleAtFixedRate(this::performHealthCheck, 5, 5, TimeUnit.MINUTES);
        
        // Memory monitoring every 10 minutes
        scheduler.scheduleAtFixedRate(this::logMemoryUsage, 10, 10, TimeUnit.MINUTES);
        
        LogUtil.info("✓ Server monitoring started");
    }
    
    /**
     * Performs server health check
     */
    private void performHealthCheck() {
        try {
            // Test database connection
            HibernateUtil.getSessionFactory().getCurrentSession();
            
            // Test RMI registry
            registry.list();
            
            LogUtil.debug("Health check passed - Server is healthy");
        } catch (Exception e) {
            LogUtil.error("Health check failed", e);
        }
    }
    
    /**
     * Logs current memory usage
     */
    private void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        LogUtil.info(String.format("Memory Usage - Used: %d MB, Free: %d MB, Total: %d MB, Max: %d MB",
                usedMemory / 1024 / 1024,
                freeMemory / 1024 / 1024,
                totalMemory / 1024 / 1024,
                maxMemory / 1024 / 1024));
    }
    
    /**
     * Adds shutdown hook for graceful cleanup
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogUtil.info("Shutdown signal received...");
            shutdown();
        }));
    }
    
    /**
     * Starts interactive console for server management
     */
    private void startInteractiveConsole() {
        Scanner scanner = new Scanner(System.in);
        LogUtil.info("Interactive console started. Type 'help' for commands.");
        
        while (isRunning) {
            try {
                System.out.print("server> ");
                String input = scanner.nextLine().trim().toLowerCase();
                
                switch (input) {
                    case "help":
                        printConsoleHelp();
                        break;
                    case "status":
                        printServerStatus();
                        break;
                    case "stats":
                        printServerStatistics();
                        break;
                    case "memory":
                        logMemoryUsage();
                        break;
                    case "health":
                        performHealthCheck();
                        break;
                    case "services":
                        listServices();
                        break;
                    case "clients":
                        listConnectedClients();
                        break;
                    case "gc":
                        System.gc();
                        LogUtil.info("Garbage collection requested");
                        break;
                    case "shutdown":
                    case "exit":
                    case "quit":
                        LogUtil.info("Shutdown command received");
                        isRunning = false;
                        break;
                    case "":
                        // Empty input, do nothing
                        break;
                    default:
                        System.out.println("Unknown command: " + input + ". Type 'help' for available commands.");
                }
            } catch (Exception e) {
                LogUtil.error("Console command error", e);
            }
        }
        
        scanner.close();
        shutdown();
    }
    
    /**
     * Prints console help
     */
    private void printConsoleHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  help      - Show this help message");
        System.out.println("  status    - Show server status");
        System.out.println("  stats     - Show server statistics");
        System.out.println("  memory    - Show memory usage");
        System.out.println("  health    - Perform health check");
        System.out.println("  services  - List registered services");
        System.out.println("  clients   - List connected clients");
        System.out.println("  gc        - Force garbage collection");
        System.out.println("  shutdown  - Shutdown the server");
        System.out.println();
    }
    
    /**
     * Prints current server status
     */
    private void printServerStatus() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            SERVER STATUS              ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Version: " + String.format("%-30s", SERVER_VERSION) + " ║");
        System.out.println("║ Host: " + String.format("%-33s", SERVER_HOST) + " ║");
        System.out.println("║ Port: " + String.format("%-33s", SERVER_PORT) + " ║");
        System.out.println("║ Status: " + String.format("%-31s", isRunning ? "RUNNING" : "STOPPED") + " ║");
        System.out.println("║ Database: " + String.format("%-29s", "CONNECTED") + " ║");
        System.out.println("║ Services: " + String.format("%-29s", "7 ACTIVE") + " ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
    
    /**
     * Prints server statistics
     */
    private void printServerStatistics() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           SERVER STATISTICS           ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Memory Used: " + String.format("%-25s", usedMemory + " MB") + " ║");
        System.out.println("║ Memory Free: " + String.format("%-25s", freeMemory + " MB") + " ║");
        System.out.println("║ Memory Total: " + String.format("%-24s", totalMemory + " MB") + " ║");
        System.out.println("║ Uptime: " + String.format("%-30s", getUptime()) + " ║");
        System.out.println("║ Active Threads: " + String.format("%-22s", Thread.activeCount()) + " ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
    
    /**
     * Lists registered services
     */
    private void listServices() {
        try {
            String[] services = registry.list();
            System.out.println("\nRegistered Services:");
            for (String service : services) {
                System.out.println("  ✓ " + service);
            }
            System.out.println();
        } catch (Exception e) {
            LogUtil.error("Failed to list services", e);
        }
    }
    
    /**
     * Lists connected clients (placeholder - would need client tracking)
     */
    private void listConnectedClients() {
        System.out.println("\nConnected Clients:");
        System.out.println("  (Client tracking not implemented yet)");
        System.out.println();
    }
    
    /**
     * Gets server uptime
     */
    private String getUptime() {
        // This is a simplified version - you might want to track actual start time
        return "Running";
    }
    
    /**
     * Tests database connection only
     */
    public void testDatabaseConnection() {
        try {
            System.out.println("Testing database connection...");
            HibernateUtil.getSessionFactory();
            System.out.println("✓ Database connection successful!");
            HibernateUtil.getSessionFactory().close();
        } catch (Exception e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Creates default admin user if no users exist
     */
    private void createDefaultAdminIfNeeded() {
        try {
            LogUtil.info("Checking for default admin user...");
            userService.createDefaultAdmin();
        } catch (Exception e) {
            LogUtil.warn("Could not create default admin user: " + e.getMessage());
        }
    }
    
    /**
     * Gracefully shuts down the server
     */
    public void shutdown() {
        LogUtil.info("Shutting down Business Management Server...");
        isRunning = false;
        
        try {
            // Stop monitoring
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                LogUtil.info("✓ Monitoring stopped");
            }
            
            // Unexport services
            if (customerService != null) {
                UnicastRemoteObject.unexportObject(customerService, true);
            }
            if (productService != null) {
                UnicastRemoteObject.unexportObject(productService, true);
            }
            if (supplierService != null) {
                UnicastRemoteObject.unexportObject(supplierService, true);
            }
            if (orderService != null) {
                UnicastRemoteObject.unexportObject(orderService, true);
            }
            if (invoiceService != null) {
                UnicastRemoteObject.unexportObject(invoiceService, true);
            }
            if (paymentService != null) {
                UnicastRemoteObject.unexportObject(paymentService, true);
            }
            if (userService != null) {
                UnicastRemoteObject.unexportObject(userService, true);
            }
            LogUtil.info("✓ Services unexported");
            
            // Close Hibernate SessionFactory
            HibernateUtil.getSessionFactory().close();
            LogUtil.info("✓ Database connections closed");
            
            LogUtil.info("✓ Server shutdown completed successfully");
            
        } catch (Exception e) {
            LogUtil.error("Error during server shutdown", e);
        }
    }
    
    /**
     * Prints usage information
     */
    private static void printUsage() {
        System.out.println("Business Management Server v" + SERVER_VERSION);
        System.out.println("\nUsage: java BusinessMgtServerController [options]");
        System.out.println("\nOptions:");
        System.out.println("  --help, -h     Show this help message");
        System.out.println("  --version, -v  Show version information");
        System.out.println("  --test, -t     Test database connection only");
        System.out.println("\nWith no options, starts the server normally.");
    }
    
    /**
     * Prints version information
     */
    private static void printVersion() {
        System.out.println("Business Management Server");
        System.out.println("Version: " + SERVER_VERSION);
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
    }
}