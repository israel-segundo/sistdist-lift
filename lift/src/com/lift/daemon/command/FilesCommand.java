
package com.lift.daemon.command;

import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.util.Map;
import java.util.Set;

public class FilesCommand implements Command{

    private RepositoryDAO repositoryDatabase         = null;
  
    public FilesCommand(RepositoryDAO repositoryDatabase){
        this.repositoryDatabase = repositoryDatabase;
    }
    
    @Override
    public Result execute() {
        
        /*
     *  List all files in local repository
         */
        System.out.println("[ INFO ] Repo: Listing files in local repo...");

        repositoryDatabase.reload();

        // TODO: sort the list by date added
        Set<Map.Entry<String, RepositoryFile>> fileSet = repositoryDatabase.getFilesMap().entrySet();
        // TODO: make the output fixed for the size of the longest entry
        StringBuilder sb = new StringBuilder();
        
        String format = "%-30s%-20s%-20s%-30s%-20s\n";
        sb.append( String.format(format, "LOCATION", "SIZE", "FILE ID", "DATE ADDED", "HITS"));

        fileSet.forEach((entry) -> {
            String location = entry.getValue().getName();
            String size = entry.getValue().getSize() + "";
            String fileID = entry.getValue().getGUID();
            String dateAdded = entry.getValue().getDateAdded();
            String hits = entry.getValue().getHits() + "";
            sb.append( String.format(format, location, size, fileID, dateAdded, hits));
        });
             

        
        Result result = new Result();
        result.setResult(sb.toString());
        result.setReturnCode(0);
        result.setMessage("some");
        
        return result;
    }
    
    
}
