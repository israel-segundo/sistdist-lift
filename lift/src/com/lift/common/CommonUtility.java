package com.lift.common;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

/**
 * This class has the common utility functions for all other classes.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class CommonUtility {
    
    public static String generateDate() {
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(c.getTime());
        
        return date;
    }
    
        
    // https://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java
    public static String generateHash(String name) {
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
    
    
    public static String generateGUID(int start, int end) {
        return UUID.randomUUID().toString().substring(start, end);
    }
}
