package com.lift.daemon.command;

import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.File;

/**
 * This class handles the internal META operation.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class MetaCommand implements LiftCommand {
    
    private String fileID                    = null;
    private RepositoryDAO repositoryDatabase = null;

    public MetaCommand(String fileID, RepositoryDAO repositoryDatabase) {
        this.fileID             = fileID;
        this.repositoryDatabase = repositoryDatabase;
    }

    @Override
    public Result execute() {
        Result result = new Result();
        byte[] data   = null;
        
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
        
        result.setReturnCode(0);
        result.setResult(file);
        
        return result;
        
    }
    
}
