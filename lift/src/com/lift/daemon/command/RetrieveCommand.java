package com.lift.daemon.command;

import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class RetrieveCommand implements LiftCommand {
    
    private String fileID                    = null;
    private RepositoryDAO repositoryDatabase = null;

    public RetrieveCommand(String fileID, RepositoryDAO repositoryDatabase) {
        this.fileID = fileID;
        this.repositoryDatabase = repositoryDatabase;
    }

    @Override
    public Result execute() {
        Result result = new Result();
        byte[] data = null;
        
        RepositoryFile file = repositoryDatabase.getFileByID(fileID);
        
        // Check if file is shared
        if(file == null) {
            // File is not in repo, and therefore not shared.
            result.setReturnCode(1);
            result.setMessage("Daemon: Could not retrieve the file. The file is no longer being shared.");
            
            return result;
        }
        
        // Check if file exists
        String fileName = file.getName();
        File fileShared = new File(fileName);
        
        if(! fileShared.exists()) {
            result.setReturnCode(2);
            result.setMessage("Daemon: Could not retrieve the file. The file is no longer available.");
            
            return result;
        }
        
        result = readFile(fileName);
        
        return result;
    }
    
    public static Result<byte[]> readFile(String filename) {
        Result result = new Result();
    	File file     = new File(filename);   
        byte[] b      = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            
            result.setReturnCode(0);
            result.setResult(b);
         } catch (IOException e1) {
              System.out.println("[ ERROR ] Could not read the requested file: " + filename);
              result.setReturnCode(1);
              result.setMessage("Daemon: Could not read the file from client location.");
         }
        
        return result;
    }
}
