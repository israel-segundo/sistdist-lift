package com.lift.daemon.command;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import com.lift.daemon.Daemon;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class RetrieveCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(RetrieveCommand.class, AppConfig.logFilePath + File.separator + "lift.log");
    
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
            logger.error("Could not read the requested file: " + fileName);
            result.setReturnCode(1);
            result.setMessage("Daemon: Could not read the file from client location.");
        }
        
        
        
        return result;
    }
    
}
