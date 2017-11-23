package com.lift.daemon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lift.common.AppConfig;
import com.lift.common.Logger;
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

    private static final Logger logger  = new Logger(RepositoryDAO.class, AppConfig.logFilePath + File.separator + "lift.log");
    
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

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
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
    
    public RepositoryFile getFileByID(String fileID) {
        RepositoryFile file = null;
        
        this.reload();
        if (filesMap.containsKey(fileID)) {
            file = filesMap.get(fileID);
        } 
        
        return file;
    }

    public boolean reload() {
        boolean isLoaded = false;
        logger.info("Repo-Database: Loading repository state from file...");
        
        if(!databaseFile.exists()){
            logger.error("Repository file does not exist.");
            return true;
        }
        
        try (Reader reader = new FileReader(databaseFile)) {
            
            filesMap = gson.fromJson(reader, type);
            logger.info("Repo-Database: Repository finished loading.");
            isLoaded = true;
            
        } catch (IOException ex) {
            logger.error("Repo-Database: Error when loading the repository state.");
            isLoaded = false;
        }
        
        return isLoaded;
    }
    
    public boolean commit() {
        boolean isSaved = false;
        logger.info("Repo-Database: Saving repository state to file ...");
        
        try (Writer writer = new FileWriter(databaseFile)) {
            
            fileCount = filesMap.size();
            gson.toJson(filesMap, writer);
            isSaved = true;
            
            logger.info("Repo-Database: Saving repository completed.");
            return isSaved;
            
        } catch (IOException ex) {
            logger.error("Repo-Database: Error when saving the repository state.");
            return isSaved;
        }
    }
}
