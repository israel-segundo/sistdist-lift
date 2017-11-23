package com.lift.daemon;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import com.lift.common.ServerConsumer;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the HeartBeat updated sent to Lift service for QoS analysis.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class HearthBeatService implements Runnable {

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
            
            logger.info("Connected to server");
            
            // Reload databases
            repositoryDatabase.reload();
            sessionDatabase.reload();
            
            // Send heartbeat signal
            boolean heartbeatAcknowledged = serverConsumer.sendHeartBeat(sessionDatabase.getSession().getGUID(), 
                                                                         repositoryDatabase.getFileCount());
            
            logger.info("Was heartbeat ack? " + heartbeatAcknowledged );
            
            if(heartbeatAcknowledged){
                logger.info("Heartbeat signal was acknowledged by the server");
                Daemon.isConnected = true;
                
            }else{
                logger.info("Server did not receive heartbeat");
                Daemon.isConnected = false;
            }
            
            
            saveConnectionStatus();
            
            
            logger.info(String.format("Attempting to sleep current heartbeat thread for %d seconds", refreshRateInSeconds));
            
            try{
                TimeUnit.SECONDS.sleep(refreshRateInSeconds);

            } catch(Exception ex){
                logger.error("Cannot sleep heartbeat thread!");
                ex.printStackTrace();
            }
        }
    }
    
    private void saveConnectionStatus(){
        
        logger.info("Attempting saving connection status ...");
        sessionDatabase.getSession().setIsConnected(Daemon.isConnected);
        sessionDatabase.commit();

    }
}
