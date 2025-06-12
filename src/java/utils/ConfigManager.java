package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static ConfigManager instance;
    private Properties properties;
    
    // Private constructor to implement Singleton pattern
    private ConfigManager() {
        loadProperties();
    }
    
    /**
     * Singleton pattern - only one instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * Load configuration file .env only once
     */
    private void loadProperties() {
        properties = new Properties();
        
        try (InputStream input = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config/.env")) {
            
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Cannot find .env file in config directory");
                throw new RuntimeException(".env file does not exist");
            }
            
            properties.load(input);
            LOGGER.log(Level.INFO, "Successfully loaded .env configuration file");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading .env file", e);
            throw new RuntimeException("Cannot read configuration file", e);
        }
    }
    
    /**
     * Get configuration value by key
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get configuration value with default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get integer value from configuration
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Cannot parse integer for key: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * Get boolean value from configuration
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Check if key exists
     */
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Get all properties (for debugging only)
     */
    public Properties getAllProperties() {
        return new Properties(properties); // Return copy to avoid modification
    }
}
