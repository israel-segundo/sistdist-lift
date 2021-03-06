package com.lift.common;

import com.lift.daemon.RepositoryFile;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    
    public static String getFormatForFilesCmd(Map<String, RepositoryFile> map) {
        
        if(map.isEmpty()) {
            return "%-30s%-20s%-20s%-30s%-20s\n";
        }
        
        Set<Map.Entry<String, RepositoryFile>> fileSet = map.entrySet();
        int[] elementLength                            = new int[5];
        int[] standardLength                           = {30, 20, 20, 15, 6};
        StringBuilder format                           = new StringBuilder();
        
        fileSet.forEach((entry) -> {
            int nameLen = entry.getValue().getName().length();
            elementLength[0] = (elementLength[0] > nameLen) ? elementLength[0] : nameLen;
            int sizeLen = String.valueOf(entry.getValue().getSize()).length();
            elementLength[1] = (elementLength[1] > sizeLen) ? elementLength[1] : sizeLen;
            int GUIDLen = entry.getValue().getGUID().length();
            elementLength[2] = (elementLength[2] > GUIDLen) ? elementLength[2] : GUIDLen;
            int dateLen = entry.getValue().getDateAdded().length();
            elementLength[3] = (elementLength[3] > dateLen) ? elementLength[3] : dateLen;
            int hitsLen = String.valueOf(entry.getValue().getHits()).length();
            elementLength[4] = (elementLength[4] > hitsLen) ? elementLength[4] : hitsLen;
        });
        
        for(int i = 0; i < elementLength.length; i++) {
            if(elementLength[i] >= standardLength[i]) {
                format.append("%-").append(elementLength[i] + 3).append("s");
            } else {
                format.append("%-").append(standardLength[i]).append("s");
            }
        }
        format.append("\n");
        
        return format.toString();
    }
    
    public static String encodeUFL(String clientGUID, String fileID) {
        String decodedUFL = clientGUID + ":" + fileID;
        byte[] e = Base64.getEncoder().encode(decodedUFL.getBytes());
        String encodedUFL = new String(e);
        
        return encodedUFL;
    }
    
    public static String[] decodeUFL(String ufl) {
        byte[] decodedBytes = Base64.getDecoder().decode(ufl);
        String[] decodedUFL = new String(decodedBytes).split(":");
        
        return decodedUFL;
    }
    
    public static boolean isUFLValid(String ufl) {
        if (ufl == null) return false;
        
        if (ufl.length() != 60) return false;
        
        String[] decodedUFL = decodeUFL(ufl.trim());
        
        if (decodedUFL != null &&
            decodedUFL[0].length() != 32 &&
            decodedUFL[1].length() != 12)
            return false;
        
        return true;
    }
    
    // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return String.format("%d B", bytes);
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    public static String humanReadableDaysAgo(String date) {
        String timeAgo = date;
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            Date past = dateFormat.parse(date);
            Date now = new Date();
            long difference = now.getTime() - past.getTime();
            if(difference/ (60 * 60 * 1000) > 23) {
                timeAgo = TimeUnit.MILLISECONDS.toDays(difference) + " days ago";
            } else if(difference/ (60 * 60 * 1000) > 0){
                timeAgo = TimeUnit.MILLISECONDS.toHours(difference) + " hours ago";
            } else if(difference/ (60 * 1000) > 1){
                timeAgo = TimeUnit.MILLISECONDS.toMinutes(difference) + " minutes ago";
            } else {
                timeAgo = "just a moment ago";
            }

        } catch (ParseException ex) {
        }
        
        return timeAgo;
    }
}
