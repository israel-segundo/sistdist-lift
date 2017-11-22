package com.lift.daemon;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class FileProviderServer implements Runnable{

    private static final Logger logger  = new Logger(FileProviderServer.class, AppConfig.logFilePath + File.separator + "lift.log");

    private ServerSocket fileServerSocket;
    private String filePath;
    
    public FileProviderServer(ServerSocket fileServerSocket, String filePath){
        this.fileServerSocket   = fileServerSocket;
        this.filePath           = filePath;
    }
    
    @Override
    public void run() {

        try {
            logger.info(String.format("File server provider for file %s is listening on port %d", filePath, fileServerSocket.getLocalPort()));
            
            Socket clientSocket = fileServerSocket.accept();

            logger.info(String.format("File server provider received a connection"));

            
            byte[] buffer = new byte[2048];

            File file               = new File(filePath);

             try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                  ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream()))
             {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bis         = new BufferedInputStream(fileInputStream);
                int length;
                while ((length = bis.read(buffer, 0, buffer.length)) != -1){
                    logger.info("Transmitting " + length + " bytes");
                    out.write(buffer, 0, length);
                }
                out.flush();
                //boolean finishedSignal = in.readBoolean();
                //logger.info("Read finished signal : " + finishedSignal);
                
                 bis.close();
                 fileInputStream.close();

             } catch (IOException e) {
                 logger.error("Could not read the requested file: " + filePath);
                 e.printStackTrace();
             }
            clientSocket.close();
            logger.info(String.format("File transmission completed. Closing the connection"));
             
        } catch (IOException e) {
            logger.error(String.format("Exception caught when trying to listen "
                    + "on port %d  or listening for a connection", fileServerSocket.getLocalPort()));
            logger.error(e.getMessage());
        }         
    }
    
}
