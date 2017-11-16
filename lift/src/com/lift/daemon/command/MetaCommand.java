package com.lift.daemon.command;

import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;


public class MetaCommand implements LiftCommand {
    
    private String fileID                    = null;
    private RepositoryDAO repositoryDatabase = null;

    public MetaCommand(String fileID, RepositoryDAO repositoryDatabase) {
        this.fileID = fileID;
        this.repositoryDatabase = repositoryDatabase;
    }

    @Override
    public Result execute() {
        Result<RepositoryFile> result = new Result();
        
        RepositoryFile file = repositoryDatabase.getFileByID(fileID);
        
        if(file == null) {
            result.setReturnCode(1);
            result.setMessage("[ ERROR ] No file found for ID: " + fileID);
        } else {
            result.setReturnCode(0);
            result.setResult(file);
        }
        
        return result;
    }
    
}
