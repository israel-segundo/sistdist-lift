package com.lift.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

/**
 * This class represents the User Config for Lift
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class AppConfig {
    
    private Properties properties;
    private final String configFilePath = "lift-config.properties";
    public static String logFilePath = null;
    
    public AppConfig(){
        
        properties = new Properties();

        loadPropertiesFromDisk();
    }

    
    /* 
     * This block reads microservices.properties and loads it into memory.
     */    
    private void loadPropertiesFromDisk(){
        InputStream input = null;
        
        try{
            
            input = AppConfig.class.getClassLoader().getResourceAsStream(configFilePath);
            properties.load(input);
            
        } catch(Exception exception){
            System.out.println("Error at loading configuration file: " + input);
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
