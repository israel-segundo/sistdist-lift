package com.lift.daemon.command;

import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RetrieveCommand implements LiftCommand {
    
    private String fileID                    = null;
    private RepositoryDAO repositoryDatabase = null;
    private Socket sock                      = null;

    public RetrieveCommand(String fileID, RepositoryDAO repositoryDatabase, Socket sock) {
        this.fileID             = fileID;
        this.repositoryDatabase = repositoryDatabase;
        this.sock               = sock;
    }

    @Override
    public Result execute() {
        Result result = new Result();
        byte[] buffer = new byte[1024_000];
        
        
        RepositoryFile repoFile = repositoryDatabase.getFileByID(fileID);
        String fileName         = repoFile.getName();
    	File file               = new File(fileName);
        
        try (ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream()))
        {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bis         = new BufferedInputStream(fileInputStream);
            int length;
    	    while ((length = bis.read(buffer)) > 0){
    	    	out.write(buffer, 0, length);
    	    }
            
            bis.close();
            fileInputStream.close();
            
        } catch (IOException e) {
            System.out.println("[ ERROR ] Could not read the requested file: " + fileName);
            result.setReturnCode(1);
            result.setMessage("Daemon: Could not read the file from client location.");
        }
        
        
        
        return result;
    }
    
}
