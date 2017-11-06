package com.lift.repo;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class represents a file in Lift local repository
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class RepositoryFile {
    
    private String name;
    private int hits;
    private String GUID;
    private String dateAdded;
    private long size;
    
    
    public RepositoryFile(String name) {
        this.name = name;
        this.hits = 0;
        this.GUID = generateFileID(name);
        this.dateAdded = generateDateAdded();
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
    
    public void setSize(long size) {
        this.size = size;
    }
    
    // https://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
    public static String generateFileID(String name) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(name.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().substring(0, 12);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String generateDateAdded() {
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(c.getTime());
        
        return date;
    }
}
