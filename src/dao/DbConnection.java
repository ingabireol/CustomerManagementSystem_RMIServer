package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import util.LogUtil;

/**
 * Manages database connections with connection pooling.
 * Singleton pattern is used to ensure only one connection pool exists.
 */
public class DbConnection {
    private static DbConnection instance;
    private BlockingQueue<Connection> connectionPool;
    private final String url;
    private final String user;
    private final String password;
    private final int maxPoolSize;
    
    /**
     * Private constructor to prevent direct instantiation
     * 
     * @param url Database URL
     * @param user Database username
     * @param password Database password
     * @param maxPoolSize Maximum number of connections in the pool
     */
    private DbConnection(String url, String user, String password, int maxPoolSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.connectionPool = new ArrayBlockingQueue<>(maxPoolSize);
        initializeConnectionPool();
    }
    
    /**
     * Gets the singleton instance of DbConnection
     * 
     * @return The DbConnection instance
     */
    public static synchronized DbConnection getInstance() {
        if (instance == null) {
            // Default configuration for PostgreSQL
            String url = "jdbc:postgresql://localhost:5432/business_db";
            String user = "postgres";
            String password = "postgres";
            int maxPoolSize = 10;
           
            // Read properties from file if available
            Properties props = new Properties();
            try {
                props.load(DbConnection.class.getClassLoader().getResourceAsStream("database.properties"));
                url = props.getProperty("db.url", url);
                user = props.getProperty("db.user", user);
                password = props.getProperty("db.password", password);
                maxPoolSize = Integer.parseInt(props.getProperty("db.maxPoolSize", "10"));
            } catch (Exception e) {
                LogUtil.error("Failed to load database properties. Using defaults.", e);
            }
            
            instance = new DbConnection(url, user, password, maxPoolSize);
        }
        return instance;
    }
    
    /**
     * Initializes the connection pool with connections
     */
    private void initializeConnectionPool() {
        try {
            // Load the JDBC driver
            Class.forName("org.postgresql.Driver");
            
            // Create connections and add to the pool
            for (int i = 0; i < maxPoolSize; i++) {
                Connection connection = createConnection();
                if (connection != null) {
                    connectionPool.offer(connection);
                }
            }
        } catch (ClassNotFoundException e) {
            LogUtil.error("PostgreSQL JDBC driver not found", e);
            throw new RuntimeException("PostgreSQL JDBC driver not found", e);
        }
    }
    
    /**
     * Creates a new database connection
     * 
     * @return A new Connection object
     * @throws SQLException If a database access error occurs
     */
    private Connection createConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            LogUtil.error("Failed to create database connection", e);
            return null;
        }
    }
    
    /**
     * Gets a connection from the pool
     * 
     * @return A database connection
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = connectionPool.poll();
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
            return connection;
        } catch (SQLException e) {
            LogUtil.error("Failed to get database connection from pool", e);
            throw e;
        }
    }
    
    /**
     * Returns a connection to the pool
     * 
     * @param connection The connection to return
     */
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (connection.isClosed()) {
                    // Replace with a new connection
                    connectionPool.offer(createConnection());
                } else {
                    // Reset auto-commit to default state
                    if (!connection.getAutoCommit()) {
                        connection.setAutoCommit(true);
                    }
                    connectionPool.offer(connection);
                }
            } catch (SQLException e) {
                LogUtil.error("Failed to release connection back to pool", e);
                // Try to create a replacement connection
                connectionPool.offer(createConnection());
            }
        }
    }
    
    /**
     * Closes all connections in the pool
     */
    public void closeAllConnections() {
        try {
            for (Connection connection : connectionPool) {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            }
            connectionPool.clear();
            LogUtil.info("All database connections have been closed");
        } catch (SQLException e) {
            LogUtil.error("Failed to close all connections", e);
        }
    }
}