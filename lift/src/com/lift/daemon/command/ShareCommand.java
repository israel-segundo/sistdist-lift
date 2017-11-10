
package com.lift.daemon.command;

import com.lift.daemon.Result;

public class ShareCommand implements Command {

    private String filePath;
    
    public ShareCommand(String filePath){
        this.filePath = filePath;
    }
    
    @Override
    public Result execute() {
        
        // use filepath
        
        return new Result();
    }
    
    
}
