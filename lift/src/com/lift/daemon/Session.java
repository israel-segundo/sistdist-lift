package com.lift.daemon;

import com.lift.common.CommonUtility;

/**
 * This class represents the user session in the Lift network.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class Session {
    private String GUID;
    private String dateJoined;
    private boolean isConnected;
    
    public Session() {
        this.GUID = CommonUtility.generateGUID(0, 24);
        this.dateJoined = CommonUtility.generateDate();
        this.isConnected = false;
    }
    
    public String getGUID() {
        return this.GUID;
    }
    
    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }
    
    public boolean getIsConnected() {
        return this.isConnected;
    }
    
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
