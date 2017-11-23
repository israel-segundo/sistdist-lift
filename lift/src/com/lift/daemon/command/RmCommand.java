package com.lift.daemon.command;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.File;

/**
 * This class handles the Lift RM operation:
 * 
 * $ lift rm <fileID>
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class RmCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(RmCommand.class, AppConfig.logFilePath + File.separator + "lift.log");
    
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
        
        logger.info("Removing file by ID [" + fileID + "] from repo...");
        
        repositoryDatabase.reload();
        
        if (repositoryDatabase.getFilesMap().containsKey(fileID)) {
            // Remove the entry from repo
            file = repositoryDatabase.getFilesMap().remove(fileID);
            isFileRemoved = (file != null);
            repositoryDatabase.commit();
            
        } else {
            
            logger.error("Could not find file. No such ID: " + fileID);
            return new Result(1, "Daemon: Could not find file. No such ID: " + fileID, null);
        }

        if(isFileRemoved) {
            logger.info("The file: [" + file.getName() + "] was removed from repository.");
            result = new Result(0, null, fileID);
        } else {
            logger.error("The file from ID: [" + fileID + "] was not removed from repository.");
            result = new Result(2, "Daemon: Error when trying to remove from repository.", null);
        }
        
        return result;
    }
    
}
