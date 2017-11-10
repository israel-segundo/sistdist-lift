package com.lift.common;


/**
 * Enum for Lift operations.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public enum LiftCommand {
    add("add"), 
    files("files"), 
    get("get"), 
    id("id"), 
    rm("id"), 
    share("share"), 
    ufl("ufl"), 
    version("version"), 
    help("--help");
    
    public final String value;
    
    private LiftCommand(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
}
