
package com.lift.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final String ERROR = ANSI_RED + "ERROR" + ANSI_RESET;
    private final String INFO  = ANSI_WHITE + "INFO" + ANSI_RESET;
    private final String WARN  = ANSI_YELLOW + "WARN" + ANSI_RESET;
    
    private Class sourceClass = null;
    
    public Logger(Class className){
        
        if(className!=null){
            this.sourceClass = className;
        } else{
            this.sourceClass = Logger.class;
        }
    }
    
    public void error(String message){
        trace(ERROR, message);
    }
    
    public void info(String message){
        trace(INFO, message);
    }
    
    public void warn(String message){
        trace(WARN, message);
    }
    
    private void trace(String type, String message){
        
        Date date = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(date);        
        
        System.out.println(String.format("%s %-10s %-20s: %s", currentTime, "[" + type + "]", sourceClass.getSimpleName() , message));
    }
}
