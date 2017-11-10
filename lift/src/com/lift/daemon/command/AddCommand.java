
package com.lift.daemon.command;

import com.lift.common.CommonUtility;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.File;
import java.util.Map;
import java.util.Set;

public class AddCommand implements Command{
    
    private RepositoryDAO repositoryDatabase    = null;
    private String filePath                     = null;
  
    public AddCommand(String filePath, RepositoryDAO repositoryDatabase){
        this.filePath = filePath;
        this.repositoryDatabase = repositoryDatabase;
    }
    
    @Override
    public Result execute() {
        
        Result result       = new Result();
        File file           = new File(filePath);
        boolean isFileAdded = false;
        
        System.out.println("[ INFO ] Repo: Adding file [" + filePath + "] to repo...");
        
        if(!file.exists()) {
            
            System.out.println("[ ERROR ] Repo: Could not add the file to repository. No such file: " + filePath);
            
        } else {
            
            repositoryDatabase.reload();
            
            String fileID = CommonUtility.generateHash(filePath);
            
            // Check if the file has already been added
            if(repositoryDatabase.getFilesMap().containsKey(fileID)) {
                System.out.println("[ INFO ] Repo: The file: [" + filePath + "] is already in repository.");
            }
            
            RepositoryFile repoFile = new RepositoryFile(filePath);
            repoFile.setSize(file.length());
            
            repositoryDatabase.getFilesMap().put(repoFile.getGUID(), repoFile);
            isFileAdded = repositoryDatabase.commit();
            
        }
        
        if(isFileAdded) {
            result.setMessage("[ INFO ] Repo: File added to repository: " + filePath);
            result.setReturnCode(0);
            
        } else {
            result.setMessage("[ ERROR ] Repo: File could not be added to repository: " + filePath);
            result.setReturnCode(1);
        }
        
        System.out.println(result.getMessage());
        
        return result;
    }
        
}
