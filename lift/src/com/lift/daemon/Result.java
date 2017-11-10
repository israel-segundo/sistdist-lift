
package com.lift.daemon;

import java.io.Serializable;


public class Result<T> implements Serializable{
    
    private int returnCode;
    private String message;
    private T result;

    public Result(){
        
    }
    
    public Result(int returnCode, String message, T result) {
        this.returnCode = returnCode;
        this.message = message;
        this.result = result;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
    
    
}
