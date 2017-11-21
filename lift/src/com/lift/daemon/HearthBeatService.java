
package com.lift.daemon;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import com.lift.common.ServerConsumer;
import java.io.File;

public class HearthBeatService implements Runnable{

    private static Logger logger = new Logger(HearthBeatService.class, AppConfig.logFilePath + File.separator + "lift.log");
    private AppConfig appConfig;
    
    private int refreshRateInSeconds = 15;
    private SessionDAO sessionDatabase;
    private RepositoryDAO repositoryDatabase;
    
    public HearthBeatService(SessionDAO sessionDatabase, RepositoryDAO repositoryDatabase){
    
        appConfig = new AppConfig();
        
        refreshRateInSeconds    = Integer.parseInt(appConfig.getProperty("lift.daemon.heartbeat", Integer.toString(refreshRateInSeconds)));
        this.sessionDatabase    = sessionDatabase;
        this.repositoryDatabase = repositoryDatabase;
    }
    
    @Override
    public void run() {
        // Send heartbeat
        while(true){
            
            // Connect with server
            ServerConsumer serverConsumer = null;
            
            try{
                serverConsumer = new ServerConsumer(); 
                
            }catch(Exception ex){
                logger.error("Error at connecting with lift-server");
                ex.printStackTrace();
                
                Daemon.isConnected = false;
            }
            
            // Reload databases
            this.repositoryDatabase.reload();
            this.sessionDatabase.reload();
            
            // Send heartbeat signal
            boolean heartbeatAcknowledged = serverConsumer.sendHeartBeat(sessionDatabase.getSession().getGUID(), 
                                                                         this.repositoryDatabase.getFileCount());
            
            if(heartbeatAcknowledged){
                logger.info("Heartbeat signal was acknowledged by the server");
                Daemon.isConnected = true;
                
            }else{
                logger.info("Server did not receive heartbeat");
                Daemon.isConnected = false;
            }
            
            saveConnectionStatus();
            
            
            try{
                Thread.currentThread().wait(refreshRateInSeconds * 1000);
                
            } catch(Exception ex){
                logger.error("Cannot sleep heartbeat thread!");
                ex.printStackTrace();
            }
        }
    }
    
    private void saveConnectionStatus(){
        
        this.sessionDatabase.getSession().setIsConnected(Daemon.isConnected);
        this.sessionDatabase.commit();
    }
}
