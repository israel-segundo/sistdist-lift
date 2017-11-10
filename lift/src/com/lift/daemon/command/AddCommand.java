
package com.lift.daemon.command;

import com.lift.common.CommonUtility;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.File;


public class AddCommand implements Command {
    
    private RepositoryDAO repositoryDatabase    = null;
    private String filePath                     = null;
  
    public AddCommand(String filePath, RepositoryDAO repositoryDatabase){
        this.filePath = filePath;
        this.repositoryDatabase = repositoryDatabase;
    }
    
    @Override
    public Result execute() {
        
        Result result           = new Result();
        File file               = new File(filePath);
        boolean isFileAdded     = false;
        RepositoryFile repoFile = null;
        
        System.out.println("[ INFO ] Repo: Adding file [" + filePath + "] to repo...");
        
        if(!file.exists()) {
            System.out.println("[ INFO ] Repo: File [" + filePath + "] does not exist.");
            return new Result(1, "Daemon: Could not add the file to repository. No such file: " + filePath, null);
            
        } else {
            
            repositoryDatabase.reload();
            
            String fileID = CommonUtility.generateHash(filePath);
            
            // Check if the file has already been added
            if(repositoryDatabase.getFilesMap().containsKey(fileID)) {
                System.out.println("[ INFO ] Repo: File [" + filePath + "] is already in repo.");
                return new Result(2, "Daemon: The file is already in repository.", null);
            }
            
            repoFile = new RepositoryFile(filePath);
            repoFile.setSize(file.length());
            
            repositoryDatabase.getFilesMap().put(repoFile.getGUID(), repoFile);
            isFileAdded = repositoryDatabase.commit();
            
        }
        
        if(isFileAdded) {
            result.setResult(repoFile.getGUID());
            result.setReturnCode(0);
            
        } else {
            result.setMessage("Daemon: Error when saving the repository state.");
            result.setReturnCode(1);
        }
        
        
        return result;
    }
        
}
