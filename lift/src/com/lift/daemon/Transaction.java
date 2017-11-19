
package com.lift.daemon;

import java.io.Serializable;

public class Transaction implements Serializable{

    protected String operation;
    protected String [] parameters;
    
    public Transaction(){
        
    }
 
    public Transaction(String operation, String [] parameters){
        this.operation = operation;
        this.parameters = parameters;
    }
    
    public String [] getParameters(){
        return this.parameters;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }    
}
