package com.lift.daemon;

import com.lift.common.CommonUtility;
import java.io.Serializable;

/**
 * This class represents the user session in the Lift network.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class SessionFile implements Serializable {
    private String GUID;
    private String dateJoined;
    private boolean isConnected;
    private String sharedDirRoute;
    private int daemonPort;
    
    public SessionFile() {
        this.GUID = CommonUtility.generateGUID(0, 32);
        this.dateJoined = CommonUtility.generateDate();
        this.isConnected = false;
        this.daemonPort = 0;
    }

    public int getDaemonPort() {
        return daemonPort;
    }

    public void setDaemonPort(int daemonPort) {
        this.daemonPort = daemonPort;
    }
   
    public String getGUID() {
        return this.GUID;
    }
    
    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }
    
    public String getSharedDirRoute() {
        return this.sharedDirRoute;
    }
    
    public void setSharedDirRoute(String sharedDirRoute) {
        this.sharedDirRoute = sharedDirRoute;
    }
    
    public boolean getIsConnected() {
        return this.isConnected;
    }
    
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
