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
import java.util.Map;

/**
 * This DAO class handles the session persistence in the local client.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class SessionDAO {
    private File sessionFile    = null;
    private Session session     = null;
    private Gson gson           = null;
    
    public SessionDAO(File sessionFile){
        this.sessionFile = sessionFile;
        this.session     = Session.getInstance();
        this.gson        = new Gson();
    }
    
    public Session getSession() {
        return session;
    }
    
    public void reload() {
        try (Reader reader = new FileReader(sessionFile)) {
            
            session = gson.fromJson(reader, Session.class);
            
        } catch (IOException ex) {
            System.out.println("[ ERROR ] Session: Error when loading the user session.");
        }
    }
    
    public boolean commit(Session session) {
        boolean isSaved = false;
        
        try (Writer writer = new FileWriter(sessionFile)) {
            
            gson.toJson(session, writer);
            isSaved = true;
            return isSaved;
            
        } catch (IOException ex) {
            System.out.println("[ ERROR ] Session: Error when saving the user session.");
            return isSaved;
        }
    }
}
