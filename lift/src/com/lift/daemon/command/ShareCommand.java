package com.lift.daemon.command;

import com.lift.common.CommonUtility;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import com.lift.daemon.SessionDAO;
import java.io.File;
import java.util.Base64;

public class ShareCommand implements LiftCommand {

    private RepositoryDAO repositoryDatabase  = null;
    private SessionDAO sessionDatabase        = null;
    private String filePath                   = null;

    public ShareCommand(String filePath, RepositoryDAO repositoryDatabase, SessionDAO sessionDatabase) {
        this.filePath           = filePath;
        this.repositoryDatabase = repositoryDatabase;
        this.sessionDatabase    = sessionDatabase;
    }
    
    @Override
    public Result execute() {
        
        Result result           = new Result();
        File file               = new File(filePath);
        boolean isFileAdded     = false;
        String ufl              = null;
        String fileID           = null;
        RepositoryFile repoFile = null;
        
        
        System.out.println("[ INFO ] SHARE-CMD: Adding file [" + filePath + "] to repo...");
        
        if(!file.exists()) {
            System.out.println("[ INFO ] SHARE-CMD: File [" + filePath + "] does not exist.");
            return new Result(1, "Daemon: Could not add the file to repository. No such file: " + filePath, null);
            
        } else {
            
            repositoryDatabase.reload();
            
            fileID = CommonUtility.generateHash(filePath);
            
            // Check if the file has already been added
            if(repositoryDatabase.getFilesMap().containsKey(fileID)) {
                System.out.println("[ INFO ] SHARE-CMD: File [" + filePath + "] is already in repo.");
                return new Result(2, "Daemon: The file is already in repository.", null);
            }
            
            repoFile = new RepositoryFile(filePath);
            repoFile.setSize(file.length());
            
            repositoryDatabase.getFilesMap().put(repoFile.getGUID(), repoFile);
            isFileAdded = repositoryDatabase.commit();
            
        }
        
        if(isFileAdded) {
            fileID = repoFile.getGUID();
            System.out.println("[ INFO ] SHARE-CMD: Generating the UFL for file ID: " + fileID);
            String decodedUFL = sessionDatabase.getSession().getGUID() + ":" + fileID;
            
            byte[] e = Base64.getEncoder().encode(decodedUFL.getBytes());
            ufl = new String(e);
            System.out.println("[ INFO ] SHARE-CMD: The UFL is: " + ufl);
            result = new Result(0, null, ufl);
        } else {
            result.setMessage("Daemon: Error when saving the repository state.");
            result.setReturnCode(3);
        }

        return result;
    }
    
    
}
