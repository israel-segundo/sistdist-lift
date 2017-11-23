package com.lift.common;

import java.util.Arrays;

/**
 * This class handles the progress bar shown to user when doing a remote file retrieval
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ProgressBar {
    private long totalSize;
    private double square;
    private static final int BAR_LENGTH = 50;
    private double current;
    private String operation;
    
    public ProgressBar(long totalSize, String operation) {
        this.totalSize = totalSize;
        this.square = totalSize / BAR_LENGTH;
        this.current = 0;
        this.operation = operation;
    }
    
    public boolean updateProgress(long delta) {
        
        int squareCount = (int) (delta / square);
        String progress = (delta >= totalSize) ? generateString('=', squareCount) : generateString('=', squareCount) + ">";
        String readableDelta = CommonUtility.humanReadableByteCount(delta, false);
        String readableTotal = CommonUtility.humanReadableByteCount(totalSize, false);
        
        System.out.printf("%s:  [%-" + BAR_LENGTH + "s]  %12s / %-12s\r", operation, progress, readableDelta, readableTotal);

        return delta < totalSize;
    }
    
    public static String generateString(char value, int length) {
        char[] charArray = new char[length];
        Arrays.fill(charArray, value);
        String str = new String(charArray);
        
        return str;
    }
}
