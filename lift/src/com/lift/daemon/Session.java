package com.lift.daemon;

import com.lift.common.CommonUtility;

/**
 * This singleton class represents the user session in the Lift network.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class Session {
    private String GUID;
    private String dateJoined;
    private boolean isConnected;
    
    private static Session session = new Session();
    
    private Session() {
        this.GUID = CommonUtility.generateGUID(0, 24);
        this.dateJoined = CommonUtility.generateDate();
        this.isConnected = false;
    }
    
    public static Session getInstance() {
        return session;
    }
    
    public String getGUID() {
        return session.GUID;
    }
    
    public void setDateJoined(String dateJoined) {
        session.dateJoined = dateJoined;
    }
    
    public boolean getIsConnected() {
        return session.isConnected;
    }
    
    public void setIsConnected(boolean isConnected) {
        session.isConnected = isConnected;
    }
}
