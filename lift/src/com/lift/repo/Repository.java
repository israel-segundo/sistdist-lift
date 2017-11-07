package com.lift.repo;

import java.io.File;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class handles the Lift local repository operations.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class Repository {
    
    private RepositoryDAO database         = null;
    private String sessionGUID             = null;
    private static File databaseFileRoute  = new File("repo.json");
    private static File sessionFileRoute   = null;
    
    
    public static void main(String[] args) {
        Repository repo =  new Repository();
        
        repo.addFile("/Users/agarcia/.profile");
        repo.listFiles();
    }
    
    
    public Repository() {
        initializeDatabase();
    }
    
    public boolean addFile(String filePath) {
        File file           = new File(filePath);
        boolean isFileAdded = false;
        
        System.out.println("[ INFO ] Repo: Adding file [" + filePath + "] to repo...");
        
        if(!file.exists()) {
            
            System.out.println("[ ERROR ] Repo: Could not add the file to repository. No such file: " + filePath);
            return isFileAdded;
            
        } else {
            
            database.reload();
            
            String fileID = RepositoryFile.generateFileID(filePath);
            
            // Check if the file has already been added
            if(database.getFilesMap().containsKey(fileID)) {
                System.out.println("[ INFO ] Repo: The file: [" + filePath + "] is already in repository.");
                return isFileAdded;
            }
            
            RepositoryFile repoFile = new RepositoryFile(filePath);
            repoFile.setSize(file.length());
            
            database.getFilesMap().put(repoFile.getGUID(), repoFile);
            isFileAdded = database.commit();
            
        }
        
        if(isFileAdded) {
            System.out.println("[ INFO ] Repo: File added to repository: " + filePath);
        } else {
            System.out.println("[ ERROR ] Repo: File could not be added to repository: " + filePath);
        }
        
        return isFileAdded;
    }
    
    public boolean removeFile(String filePath) {
        System.out.println("[ INFO ] Repo: Removing file [" + filePath + "] from repo...");
        return false;
    }
    
    public void listFiles() {
        
        System.out.println("[ INFO ] Repo: Listing files in local repo...");
        
        database.reload();
        
        // TODO: sort the list by date added
        Set<Map.Entry<String, RepositoryFile>> fileSet = database.getFilesMap().entrySet();
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
    
    public String getSessionID() {
        return null;
    }
    
    private void initializeDatabase() {
        
        System.out.println("[ INFO ] Repository file is: " + databaseFileRoute.getAbsolutePath());
        
        database = new RepositoryDAO(databaseFileRoute);
        
        //database.commit();
    }
    
    
    
    // TODO: below operations belong to the client. Move to client once its ready.
    // ------------------------------------------------
    private void generateSessionGUID() {
        // TODO: get session value from file
        sessionGUID = UUID.randomUUID().toString().substring(0, 24);
    }
    
    public String getSessionGUID() {
        return sessionGUID;
    }
    
    //
    // Constructs the Universal File Locator (UFL) in the following format:
    //      sessionGUID:fileID | base64
    //
    private String getUFL(String fileID) {
        String UFL = null;
        
        if(database.getFilesMap().keySet().contains(fileID)) {
            
            sessionGUID = getSessionGUID();
            String decodedUFL = sessionGUID + ":" + fileID;
            
            byte[] e = Base64.getEncoder().encode(decodedUFL.getBytes());
            UFL = new String(e);
            
        } else {
            
            System.out.println("[ ERROR ] Repo: Failed to get the UFL. No such ID: " + fileID);
            
        }
        
        return UFL;
    }
    
    public boolean shareFile(String fileID) {
        return false;
    }
    // ------------------------------------------------
    
}
