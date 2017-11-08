package com.lift.daemon;

import com.lift.common.CommonUtility;
import java.io.File;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

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
    
    private static final String CLIENT_VERSION      = "1.0.0";
    private static final String CLIENT_BUILD_DATE   = "Thursday 7 Nov, 2017";
    private static final String SERVER_VERSION      = "1.0.0";
    private static final String SERVER_BUILD_DATE   = "Thursday 7 Nov, 2017";
    private static final File SESSION_FILE_ROUTE     = new File("session.json");
    private static final File REPOSITORY_FILE_ROUTE  = new File("repo.json");
    
    private RepositoryDAO repositoryDatabase         = null;
    private SessionDAO sessionDatabase               = null;
    
    
    public Daemon() {
        initSession();
        initRepository();
    }
    
    
    /*
    *  Add a file to local repository
    */
    public boolean add(String filePath) {
        File file           = new File(filePath);
        boolean isFileAdded = false;
        
        System.out.println("[ INFO ] Repo: Adding file [" + filePath + "] to repo...");
        
        if(!file.exists()) {
            
            System.out.println("[ ERROR ] Repo: Could not add the file to repository. No such file: " + filePath);
            return isFileAdded;
            
        } else {
            
            repositoryDatabase.reload();
            
            String fileID = CommonUtility.generateHash(filePath);
            
            // Check if the file has already been added
            if(repositoryDatabase.getFilesMap().containsKey(fileID)) {
                System.out.println("[ INFO ] Repo: The file: [" + filePath + "] is already in repository.");
                return isFileAdded;
            }
            
            RepositoryFile repoFile = new RepositoryFile(filePath);
            repoFile.setSize(file.length());
            
            repositoryDatabase.getFilesMap().put(repoFile.getGUID(), repoFile);
            isFileAdded = repositoryDatabase.commit();
            
        }
        
        if(isFileAdded) {
            System.out.println("[ INFO ] Repo: File added to repository: " + filePath);
        } else {
            System.out.println("[ ERROR ] Repo: File could not be added to repository: " + filePath);
        }
        
        return isFileAdded;
    }
    
    
    /*
    *  List all files in local repository
    */
    public void files() {
        System.out.println("[ INFO ] Repo: Listing files in local repo...");
        
        repositoryDatabase.reload();
        
        // TODO: sort the list by date added
        Set<Map.Entry<String, RepositoryFile>> fileSet = repositoryDatabase.getFilesMap().entrySet();
        // TODO: make the output fixed for the size of the longest entry
        String format = "%-30s%-20s%-20s%-30s%-20s\n";
        System.out.format(format, "LOCATION", "SIZE", "FILE ID", "DATE ADDED", "HITS");

        fileSet.forEach((entry) -> {
            String location     = entry.getValue().getName();
            String size         = entry.getValue().getSize() + "";
            String fileID       = entry.getValue().getGUID();
            String dateAdded    = entry.getValue().getDateAdded();
            String hits         = entry.getValue().getHits() + "";
            System.out.format(format, location, size, fileID, dateAdded, hits);
        });
        
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
        System.out.printf("Client:\n");
        System.out.printf(" %-15s%s\n", "Version:", CLIENT_VERSION);
        System.out.printf(" %-15s%s\n", "Built:",   CLIENT_BUILD_DATE);
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
    private void initSession() {
        
        initSessionDatabase();
        
        String guid = null;
        
        if(SESSION_FILE_ROUTE.exists()) {
            // Load session from file
            sessionDatabase.reload();
            guid = sessionDatabase.getSession().getGUID();
            
        } else {
            // TODO: Connect to server...
            // if connect is successful, do below
            Session session = new Session();
            String timestamp = CommonUtility.generateDate();

            session.setIsConnected(true);
            session.setDateJoined(timestamp);
            guid = session.getGUID();
            
            sessionDatabase.setSession(session);
            sessionDatabase.commit();
        }
        
        System.out.println("[ INFO ] Session GUID: " + guid);
    }
    
    private void initRepository() {
        
        initRepositoryDatabase();
    
    }
    
    private void initSessionDatabase() {
        this.sessionDatabase = new SessionDAO(SESSION_FILE_ROUTE);
    }
    
    private void initRepositoryDatabase() {
        System.out.println("[ INFO ] Repository file is: " + REPOSITORY_FILE_ROUTE.getAbsolutePath());
        repositoryDatabase = new RepositoryDAO(REPOSITORY_FILE_ROUTE);
    }
    
    private int getFilesSharedCount() {
        return repositoryDatabase.getFileCount();
    }
    
}
