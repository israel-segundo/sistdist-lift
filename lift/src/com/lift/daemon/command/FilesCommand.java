
package com.lift.daemon.command;

import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.Result;


/*
 *  List all files in local repository
 */
public class FilesCommand implements Command{

    private RepositoryDAO repositoryDatabase         = null;
  
    public FilesCommand(RepositoryDAO repositoryDatabase){
        this.repositoryDatabase = repositoryDatabase;
    }
    
    @Override
    public Result execute() {
        
        System.out.println("[ INFO ] Repo: Listing files in local repo...");

        boolean isDatabaseLoaded = repositoryDatabase.reload();
        Result result = new Result();
        
        if(isDatabaseLoaded) {
            result.setResult(repositoryDatabase.getFilesMap());
            result.setReturnCode(0);
        } else {
            result.setMessage("Daemon: Error when loading the repository state.");
            result.setReturnCode(1);
        }
        
        return result;
    }
    
    
}
