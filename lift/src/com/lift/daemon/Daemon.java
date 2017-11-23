package com.lift.daemon;

import com.lift.common.AppConfig;
import com.lift.common.CommonUtility;
import com.lift.common.Logger;
import com.lift.common.ServerConsumer;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

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
    
    public static class Sem {
        public volatile boolean isReady   = false;
        public volatile boolean terminate = false;
        public volatile int progressBarServerPort  = 0;
    }
       
    private static Logger logger        = null;

    private static File sessionFile     = null;
    private static File repositoryFile  = null;
    public static File configDirFile    = null;
    public static String sharedDirPath  = null;
    
    public static File sharedDirFile    = null;
    public static final int SUCCESS     = 0;
    
    private static RepositoryDAO repositoryDatabase  = null;
    private static SessionDAO sessionDatabase        = null;
    
    private static int portNumber                    = 0;
    private static AppConfig appConfig               = null;
    
    public static boolean isConnected                = true;
    
    // To be used for download
    public static volatile boolean isClientReady     = false;
    public static Socket localClientSocket           = null;
    public static volatile boolean terminateDownload = false;
    public static volatile int progressBarServerPort    = 0;
    public static volatile Sem sem = new Sem();
        
    public static void main(String[] args) {
        
        loadConfig();
        initConfigDirectory();
        initSession();
        initRepository();
        initSharedDirectory();
        
        //spawnHeartbeatService();
        
        ServerSocket serverSocket;
        ExecutorService service = Executors.newCachedThreadPool();

        try {
            serverSocket = new ServerSocket(portNumber);
            portNumber = serverSocket.getLocalPort();
            sessionDatabase.getSession().setDaemonPort(portNumber);
            sessionDatabase.commit();
            
            logger.info(String.format("Daemon listening on port: [%d]", portNumber));
            
            //registerIntoServer();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                logger.info("Sem from DAEMON hashcode: " + sem.hashCode());
                service.execute(new DaemonTask(sem, clientSocket, repositoryDatabase, sessionDatabase));
            }

        } catch (IOException e) {
            logger.error(String.format("Exception caught when trying to listen "
                    + "on port %d  or listening for a connection", portNumber));
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
        
        AppConfig.logFilePath = configDirFile.getAbsolutePath();
        logger = new Logger(Daemon.class, AppConfig.logFilePath + File.separator + "lift.log");
        
    }
    
    
    private static void spawnHeartbeatService(){
        
        if(repositoryDatabase.getDatabaseFile().exists()) {
            // Load session from file
            repositoryDatabase.reload();
        } else {
            
            try {
                repositoryDatabase.getDatabaseFile().createNewFile();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        HearthBeatService heartBeatService = new HearthBeatService(sessionDatabase, repositoryDatabase);
        new Thread(heartBeatService).start();
    }
    
    private static void registerIntoServer(){
        
        ServerConsumer serverConsumer = null;
        
        // Attempt to connect to lift-server
        try{
            serverConsumer = new ServerConsumer();
            
            
        } catch(Exception ex){
            logger.error("Cannot connect to lift-server");
            
            // TODO:  Notify to client about server being down
        }
        
        
        // Register current client into server
        String hostname = "";
        String ipAddress = "";
        InetAddress ip;

        try {
            ip = InetAddress.getLocalHost();
            ipAddress = ip.getHostAddress();
            hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your current Hostname : " + hostname);
 
        } catch (UnknownHostException e) {
 
            logger.error("Unable to get local ip and hostname");
            logger.error(e.getMessage());
            
        }
        
        logger.info("Session database object = " + sessionDatabase);
        logger.info("sessionDatabase object = " + sessionDatabase);
        
        
        boolean wasRegistered = serverConsumer.register(sessionDatabase.getSession().getGUID(), 
                                                        ipAddress,
                                                        hostname,
                                                        sessionDatabase.getSession().getDaemonPort(),
                                                        0);
        
        if(wasRegistered){
            logger.info("We got registered into the lift-server.");
            
            sessionDatabase.getSession().setIsConnected(true);
            sessionDatabase.commit();
            
        }else {
            logger.error("Registration to the lift-server failed.");
            
            
        }
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
            
            logger.info("sessionDatabase : " + sessionDatabase);
            logger.info("sessionDatabase.getSession() : " + sessionDatabase.getSession());
            logger.info("sessionDatabase.getGUID() : " + sessionDatabase.getSession().getGUID());
            
            guid = sessionDatabase.getSession().getGUID();
            portNumber = sessionDatabase.getSession().getDaemonPort();
                        
        } else {
                        
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
        logger.info("init session database");
        sessionDatabase = new SessionDAO(sessionFile);
        sessionDatabase.getSession().setSharedDirRoute(sharedDirPath);
        logger.info("end init session database");

    }
    
    private static void initRepositoryDatabase() {
        logger.info("init repo database");
        logger.info("Repository file is: " + repositoryFile.getAbsolutePath());
        repositoryDatabase = new RepositoryDAO(repositoryFile);
        logger.info("end init repo  database");
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
    
    public static void setLocalClientSocket(Socket sock) {
        localClientSocket = sock;
        isClientReady = true;
    }
    
}
