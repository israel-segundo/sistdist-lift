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
    
    private static final String SERVER_VERSION       = "1.0.0";
    private static final String SERVER_BUILD_DATE    = "Thursday 7 Nov, 2017";
    private static final File SESSION_FILE_ROUTE     = new File("session.json");
    private static final File REPOSITORY_FILE_ROUTE  = new File("repo.json");
    
    private static RepositoryDAO repositoryDatabase  = null;
    private static SessionDAO sessionDatabase        = null;
    
    private static final int PORT_NUMBER             = 45115;
    private static final String HOSTNAME             = "localhost";    
    
    
    public static void main(String[] args) {
        
        initSession();
        initRepository();
        
        
        ServerSocket serverSocket;
        ExecutorService service = Executors.newCachedThreadPool();

        try {
            serverSocket = new ServerSocket(PORT_NUMBER);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[ INFO ] Connection received.");
                
                service.execute(new DaemonTask(clientSocket, repositoryDatabase));
            }

        } catch (IOException e) {
            System.out.println("[ ERROR ] Exception caught when trying to listen on port "
                    + PORT_NUMBER + " or listening for a connection");
            System.out.println(e.getMessage());
        }    
    }
    
    
    
    // TODO
    public boolean get(String ufl) {
        return false;
    }
    
    
    /*
    *  Get the user GUID in the network
    */
    public String id() {
        String id = sessionDatabase.getSession().getGUID();
        String format = "\n\t%-10s%-35s\n";
        System.out.format(format, "GUID is:", id);
        return id;
    }
    
    
    /*
    *  Remove a file from local repository by ID
    */
    public boolean rm(String fileID) {
        System.out.println("[ INFO ] Repo: Removing file by ID [" + fileID + "] from repo...");
        
        boolean isFileRemoved = false;
        repositoryDatabase.reload();
        RepositoryFile file = null;
        
        if (repositoryDatabase.getFilesMap().containsKey(fileID)) {
            // Remove the entry from repo
            file = repositoryDatabase.getFilesMap().remove(fileID);
            isFileRemoved = (file != null);
            repositoryDatabase.commit();
            
        } else {
            
            System.out.println("[ ERROR ] Repo: Could not find file. No such ID: " + fileID);
            isFileRemoved = false;
            return isFileRemoved;
        }

        if(isFileRemoved) {
            System.out.println("[ INFO ] Repo: The file: [" + file.getName() + "] was removed from repository.");
        } else {
            System.out.println("[ ERROR ] Repo: The file from ID: [" + fileID + "] was not removed from repository.");
        }
        
        return isFileRemoved;
    }
    
    
    // TODO
    public boolean share(String filePath) {
        return false;
    }
    
    
    /*
    *  Get the UFL from file ID
    *
    *  Constructs the Universal File Locator (UFL) in the following format:
    *  sessionGUID:fileID | base64
    *
    */
    public String ufl(String fileID) {
        String ufl = null;
        
        repositoryDatabase.reload();
        
        if(repositoryDatabase.getFilesMap().keySet().contains(fileID)) {
            
            String decodedUFL = sessionDatabase.getSession().getGUID() + ":" + fileID;
            
            byte[] e = Base64.getEncoder().encode(decodedUFL.getBytes());
            ufl = new String(e);
            
            String format = "\n\t%-10s%-30s\n";
            System.out.format(format, "UFL is:", ufl);
            
        } else {
            
            System.out.println("[ ERROR ] Repo: Failed to get the UFL. No such ID: " + fileID);
            
        }
        
        return ufl;
    }
    
    
    /*
    *  Show the version from client and server
    */
    public String version() {
        // TODO: obtain below properties from server
        System.out.printf("\nServer:\n");
        System.out.printf(" %-15s%s\n", "Version:", SERVER_VERSION);
        System.out.printf(" %-15s%s\n", "Built:",   SERVER_BUILD_DATE);
        System.out.printf("\n");
        
        return null;
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
    }
    
    private static void initRepositoryDatabase() {
        System.out.println("[ INFO ] Repository file is: " + REPOSITORY_FILE_ROUTE.getAbsolutePath());
        repositoryDatabase = new RepositoryDAO(REPOSITORY_FILE_ROUTE);
    }
    
    private int getFilesSharedCount() {
        return repositoryDatabase.getFileCount();
    }
    
}
