package com.lift.daemon;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * This DAO class handles the session persistence in the local client.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class SessionDAO {
    private File sessionFile    = null;
    private SessionFile session = null;
    private Gson gson           = null;
    
    // This is some GSON magic!
    private Type type = new TypeToken<SessionFile>(){}.getType(); 
    
    public SessionDAO(File sessionFile){
        this.sessionFile = sessionFile;
        this.session     = new SessionFile();
        this.gson        = new Gson();
    }
    
    public SessionFile getSession() {
        return session;
    }
    
    public void setSession(SessionFile session) {
        this.session = session;
    }
    
    public void reload() {
        System.out.println("[ INFO ] Database: Loading session state from file...");
        
        try (Reader reader = new FileReader(sessionFile)) {
            
            session = gson.fromJson(reader, type);
            
            System.out.println("[ INFO ] Database: Session finished loading.");
            
        } catch (IOException ex) {
            System.out.println("[ ERROR ] Session: Error when loading the user session.");
        }
    }
    
    public boolean commit() {
        boolean isSaved = false;
        System.out.println("[ INFO ] Database: Saving session state to file ...");
        
        try (Writer writer = new FileWriter(sessionFile)) {
            
            gson.toJson(session, writer);
            isSaved = true;
            System.out.println("[ INFO ] Database: Saving session completed.");
            return isSaved;
            
        } catch (IOException ex) {
            System.out.println("[ ERROR ] Session: Error when saving the user session.");
            return isSaved;
        }
    }
}
