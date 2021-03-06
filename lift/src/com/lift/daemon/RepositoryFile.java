package com.lift.daemon;

import com.google.gson.Gson;
import com.lift.common.CommonUtility;
import java.io.Serializable;

/**
 * This class represents a file in Lift local repository
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class RepositoryFile implements Serializable {
    
    private String name;
    private int hits;
    private String GUID;
    private String dateAdded;
    private long size;
    
    
    public RepositoryFile(String name) {
        this.name = name;
        this.hits = 0;
        this.GUID = CommonUtility.generateHash(name);
        this.dateAdded = CommonUtility.generateDate();
        this.size = 0;
    }
    
    public String getGUID() {
        return GUID;
    }
    
    public String getDateAdded() {
        return dateAdded;
    }
    
    public int getHits() {
        return hits;
    }
    
    public String getName() {
        return name;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setHits(int hits) {
        this.hits = hits;
    }
    
    public int incrementHits(int increment) {
        this.hits = this.hits + increment;
        
        return hits;
    }
    
    public void setSize(long size) {
        this.size = size;
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
