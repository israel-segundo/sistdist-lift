
package com.lift.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class ApplicationConfiguration {

    private static final Logger logger = new Logger(ApplicationConfiguration.class);
    
    private Properties properties;
    private final String configFilePath = "lift-config.properties";
    
    public ApplicationConfiguration(){
        
        logger.info("Loading properties file.");
        properties = new Properties();

        loadPropertiesFromDisk();
    }

    
    /* 
     * This block reads microservices.properties and loads it into memory.
     */    
    private void loadPropertiesFromDisk(){
        InputStream input = null;
        
        try{
            input = ApplicationConfiguration.class.getClassLoader().getResourceAsStream(configFilePath);
            logger.info("Input:" + input);
            properties.load(input);
            
        } catch(Exception exception){
            logger.error("Error at loading configuration file." + input);
            exception.printStackTrace(System.err);
            
        } finally{
            if(input != null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }            
        }        
    }
    
    public Properties getProperties(){
        return this.properties;
    }
    
    public String getProperty(String propertyName){
        return getProperty(propertyName, null);
    }
    
    public String getProperty(String propertyName, String defaultValue){
        
        String propertyValue = (String)getProperties().get(propertyName);
        
        if(propertyValue != null){
            return propertyValue;
        }
        
        return defaultValue;
    }
    
    public String toString(){
        return getPropertyAsString(properties);
    }
    private String getPropertyAsString(Properties prop) {    
        
        StringWriter writer = new StringWriter();
        prop.list(new PrintWriter(writer));
        return writer.getBuffer().toString();
    }    
}
