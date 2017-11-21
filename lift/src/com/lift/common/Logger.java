
package com.lift.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static final String ANSI_RESET   = "\u001B[0m";
    public static final String ANSI_BLACK   = "\u001B[30m";
    public static final String ANSI_RED     = "\u001B[31m";
    public static final String ANSI_GREEN   = "\u001B[32m";
    public static final String ANSI_YELLOW  = "\u001B[33m";
    public static final String ANSI_BLUE    = "\u001B[34m";
    public static final String ANSI_PURPLE  = "\u001B[35m";
    public static final String ANSI_CYAN    = "\u001B[36m";
    public static final String ANSI_WHITE   = "\u001B[37m";

    private final String ERROR =  "ERROR";
    private final String INFO  =  "INFO ";
    private final String WARN  =  "WARN ";
    
    private Class sourceClass = null;
    private String logPath    = null;
    private File logFile      = null;
    
    public Logger(Class className, String logPath){
        
        this.logFile = new File(logPath);
        
        if(className != null){
            this.sourceClass = className;
        } else {
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
        
        try (FileWriter fw      = new FileWriter(logFile, true);
             BufferedWriter bw  = new BufferedWriter(fw);
             PrintWriter out    = new PrintWriter(bw)
        ){
            out.println(String.format("%s - %-5s : %-15s - %s", currentTime, type, sourceClass.getSimpleName() , message));
            System.out.println(String.format("%s - %-5s : %-15s - %s", currentTime, type, sourceClass.getSimpleName() , message));
        } catch (IOException e) {
            // nothing for now...
        }
    }
}
