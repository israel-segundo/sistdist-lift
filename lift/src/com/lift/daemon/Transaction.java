
package com.lift.daemon;

import java.io.Serializable;

public class Transaction implements Serializable{

    private String operation;
    private String parameter;
    
    public Transaction(){
        
    }
    
    public Transaction(String operation, String parameter){
        this.operation = operation;
        this.parameter = parameter;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
    
    
}
