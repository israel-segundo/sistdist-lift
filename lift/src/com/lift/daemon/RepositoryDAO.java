package com.lift.daemon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO that handles the files in local repository and its meta-data
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */

public class RepositoryDAO {

    private File databaseFile                     = null;
    private Map<String, RepositoryFile> filesMap  = null;
    private Gson gson                             = null;
    private int fileCount                         = 0;
    
    // This is some GSON magic!
    private Type type = new TypeToken<Map<String, RepositoryFile>>(){}.getType(); 
    
    public RepositoryDAO(File databaseFile){
        this.databaseFile = databaseFile;
        this.filesMap     = new HashMap<>();
        this.gson         = new Gson();
        this.fileCount    = 0;
    }
    
    public Map<String, RepositoryFile> getFilesMap() {
        return filesMap;
    }
    
    public File getDatabaseFile() {
        return databaseFile;
    }
    
    public int getFileCount() {
        return fileCount;
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    public void reload() {
        System.out.println("[ INFO ] Database: Loading repository state from file...");
        
        if(!databaseFile.exists()) return;
        
        try (Reader reader = new FileReader(databaseFile)) {
            
            filesMap = gson.fromJson(reader, type);
            System.out.println("[ INFO ] Database: Repository finished loading.");
            
        } catch (IOException ex) {
            System.out.println("[ ERROR ] Database: Error when loading the repository state.");
        }
    }
    
    public boolean commit() {
        boolean isSaved = false;
        System.out.println("[ INFO ] Database: Saving repository state to file ...");
        
        try (Writer writer = new FileWriter(databaseFile)) {
            
            fileCount = filesMap.size();
            gson.toJson(filesMap, writer);
            isSaved = true;
            
            System.out.println("[ INFO ] Database: Saving repository completed.");
            return isSaved;
            
        } catch (IOException ex) {
            System.out.println("[ ERROR ] Database: Error when saving the repository state.");
            return isSaved;
        }
    }
}
