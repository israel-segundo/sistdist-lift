package com.lift.daemon;

import com.lift.common.CommonUtility;
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
    private static final File SESSION_FILE_ROUTE     = new File("session.json");
    private static final File REPOSITORY_FILE_ROUTE  = new File("repo.json");
    public static final String SHARED_DIR_PATH       = "C:\\lift\\shared";
    public static final File SHARED_DIR_ROUTE        = new File(SHARED_DIR_PATH);
    public static final int SUCCESS                  = 0;
    
    private static RepositoryDAO repositoryDatabase  = null;
    private static SessionDAO sessionDatabase        = null;
    
    private static final int DEFAULT_PORT_NUMBER     = 45115;
    private static final String HOSTNAME             = "localhost";    
    
    private static int port;
    
    public static void main(String[] args) {
        
        port = DEFAULT_PORT_NUMBER;

        if(args.length > 1){
            
            try{
                port = Integer.parseInt(args[0]);
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
        initSession();
        initRepository();
        initSharedDirectory();
        
        ServerSocket serverSocket;
        ExecutorService service = Executors.newCachedThreadPool();

        try {
            serverSocket = new ServerSocket(port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(String.format("Daemon listening on port %d", port));
                service.execute(new DaemonTask(clientSocket, repositoryDatabase, sessionDatabase));
            }

        } catch (IOException e) {
            System.out.println("[ ERROR ] Exception caught when trying to listen on port "
                    + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }    
    }
    
    
    
    /*
    *  Private methods
    */
    private static void initSession() {
        
        initSessionDatabase();
        
        String guid = null;
        
        if(SESSION_FILE_ROUTE.exists()) {
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
        
        System.out.println("[ INFO ] Session GUID: " + guid);
    }
    
    private static void initRepository() {
        
        initRepositoryDatabase();
    }
    
    private static void initSessionDatabase() {
        sessionDatabase = new SessionDAO(SESSION_FILE_ROUTE);
        sessionDatabase.getSession().setSharedDirRoute(SHARED_DIR_PATH);
    }
    
    private static void initRepositoryDatabase() {
        System.out.println("[ INFO ] Repository file is: " + REPOSITORY_FILE_ROUTE.getAbsolutePath());
        repositoryDatabase = new RepositoryDAO(REPOSITORY_FILE_ROUTE);
    }
    
    private static void initSharedDirectory() {
        if (!SHARED_DIR_ROUTE.exists()) {
            SHARED_DIR_ROUTE.mkdirs();
        }

        int filesShared = SHARED_DIR_ROUTE.listFiles().length;
        System.out.println("[ INFO ] Shared directory is: " + SHARED_DIR_ROUTE.getAbsolutePath());
        System.out.println("[ INFO ] Sharing: " + filesShared + " files.");
    }
    
    private int getFilesSharedCount() {
        return repositoryDatabase.getFileCount();
    }
    
}
