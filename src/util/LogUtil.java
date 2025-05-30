package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for the business management server
 */
public class LogUtil {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log info message
     * 
     * @param message The message to log
     */
    public static void info(String message) {
        System.out.println("[INFO] " + LocalDateTime.now().format(formatter) + " - " + message);
    }
    
    /**
     * Log error message
     * 
     * @param message The error message to log
     */
    public static void error(String message) {
        System.err.println("[ERROR] " + LocalDateTime.now().format(formatter) + " - " + message);
    }
    
    /**
     * Log error message with exception
     * 
     * @param message The error message to log
     * @param exception The exception to log
     */
    public static void error(String message, Exception exception) {
        System.err.println("[ERROR] " + LocalDateTime.now().format(formatter) + " - " + message);
        if (exception != null) {
            exception.printStackTrace();
        }
    }
    
    /**
     * Log warning message
     * 
     * @param message The warning message to log
     */
    public static void warn(String message) {
        System.out.println("[WARN] " + LocalDateTime.now().format(formatter) + " - " + message);
    }
    
    /**
     * Log debug message
     * 
     * @param message The debug message to log
     */
    public static void debug(String message) {
        System.out.println("[DEBUG] " + LocalDateTime.now().format(formatter) + " - " + message);
    }
}