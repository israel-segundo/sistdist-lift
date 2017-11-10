package com.lift.daemon.command;

import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;


public class RmCommand implements LiftCommand {
    
    private RepositoryDAO repositoryDatabase  = null;
    private String fileID                     = null;

    public RmCommand(String fileID, RepositoryDAO repositoryDatabase) {
        this.fileID             = fileID;
        this.repositoryDatabase = repositoryDatabase;
    }

    @Override
    public Result execute() {
        Result result           = new Result();
        boolean isFileRemoved   = false;
        RepositoryFile file     = null;
        
        System.out.println("[ INFO ] RM-CMD: Removing file by ID [" + fileID + "] from repo...");
        
        repositoryDatabase.reload();
        
        if (repositoryDatabase.getFilesMap().containsKey(fileID)) {
            // Remove the entry from repo
            file = repositoryDatabase.getFilesMap().remove(fileID);
            isFileRemoved = (file != null);
            repositoryDatabase.commit();
            
        } else {
            
            System.out.println("[ ERROR ] RM-CMD: Could not find file. No such ID: " + fileID);
            return new Result(1, "Daemon: Could not find file. No such ID: " + fileID, null);
        }

        if(isFileRemoved) {
            System.out.println("[ INFO ] RM-CMD: The file: [" + file.getName() + "] was removed from repository.");
            result = new Result(0, null, fileID);
        } else {
            System.out.println("[ ERROR ] RM-CMD: The file from ID: [" + fileID + "] was not removed from repository.");
            result = new Result(2, "Daemon: Error when trying to remove from repository.", null);
        }
        
        return result;
    }
    
}
