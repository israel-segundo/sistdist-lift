package com.lift.daemon;

import com.lift.client.ClientManager;
import com.lift.common.AppConfig;
import com.lift.common.CommonUtility;
import com.lift.common.Logger;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lift Daemon
 * 
 * This class represents the Lift daemon which handles all client, and c-s operations.
 * It also handles the local repository operations.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class Daemon {
       
    private static final Logger logger  = new Logger(Daemon.class);

    private static File sessionFile     = null;
    private static File repositoryFile  = null;
    private static File configDirFile   = null;
    public static String sharedDirPath  = null;
    
    public static File sharedDirFile    = null;
    public static final int SUCCESS     = 0;
    
    private static RepositoryDAO repositoryDatabase  = null;
    private static SessionDAO sessionDatabase        = null;
    
    private static int portNumber                    = 0;
    private static AppConfig appConfig               = null;
        
    public static void main(String[] args) {
        
        loadConfig();
        initConfigDirectory();
        initSession();
        initRepository();
        initSharedDirectory();
        
        ServerSocket serverSocket;
        ExecutorService service = Executors.newCachedThreadPool();

        try {
            serverSocket = new ServerSocket(portNumber);
            
            sessionDatabase.getSession().setDaemonPort(serverSocket.getLocalPort());
            sessionDatabase.commit();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info(String.format("Daemon listening on port %d", portNumber));
                service.execute(new DaemonTask(clientSocket, repositoryDatabase, sessionDatabase));
            }

        } catch (IOException e) {
            logger.error(String.format("[ ERROR ] Exception caught when trying to listen "
                    + "on port %d  or listening for a connection",portNumber));
            logger.error(e.getMessage());
        }    
    }
    
    
    private static void loadConfig(){
    
        appConfig = new AppConfig();
        
        String configDir    = appConfig.getProperty("lift.config.dir");
        configDirFile       = new File(configDir);
        
        String sessionFilePath  = configDir + File.separator + "session.json";        
        sessionFile             = new File(sessionFilePath);
        
        String repoFilePath = configDir + File.separator + "repo.json";
        repositoryFile      = new File(repoFilePath);
        
        String sharedDir    = appConfig.getProperty("lift.shared.dir");
        sharedDirFile       = new File(sharedDir);
        
        
        
    }
    
    /*
    *  Private methods
    */
    private static void initSession() {
        
        initSessionDatabase();
        
        String guid = null;
        
        if(sessionFile.exists()) {
            // Load session from file
            sessionDatabase.reload();
            guid = sessionDatabase.getSession().getGUID();
                        
        } else {
            // TODO: Connect to server...
            // if connect is successful, do below
            SessionFile session = new SessionFile();
            String timestamp = CommonUtility.generateDate();

            session.setIsConnected(true);
            session.setDateJoined(timestamp);
            guid = session.getGUID();
            
            sessionDatabase.setSession(session);
            sessionDatabase.commit();
        }
        
        logger.info("Session GUID: " + guid);
    }
    
    private static void initRepository() {
        
        initRepositoryDatabase();
    }
    
    private static void initSessionDatabase() {
        sessionDatabase = new SessionDAO(sessionFile);
        sessionDatabase.getSession().setSharedDirRoute(sharedDirPath);
    }
    
    private static void initRepositoryDatabase() {
        logger.info("Repository file is: " + repositoryFile.getAbsolutePath());
        repositoryDatabase = new RepositoryDAO(repositoryFile);
    }
    
    private static void initConfigDirectory() {

        logger.info("Config directory is: " + configDirFile.getAbsolutePath());
        
        if (!configDirFile.exists()) {
            logger.info("Config directory does not exist. Attempting to create");
            configDirFile.mkdirs();
        }
        
        if(!configDirFile.exists()){
            logger.error("Config dir does not exist and could not be created.");
        }
    }
    
    private static void initSharedDirectory() {
        if (!sharedDirFile.exists()) {
            sharedDirFile.mkdirs();
        }

        int filesShared = sharedDirFile.listFiles().length;
        logger.info("Shared directory is: " + sharedDirFile.getAbsolutePath());
        logger.info("Sharing: " + filesShared + " files.");
    }
    
    private int getFilesSharedCount() {
        return repositoryDatabase.getFileCount();
    }
    
}
