
package com.lift.common;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

public class ServerConsumer {

    private static final Logger logger  = new Logger(ServerConsumer.class);
    private static AppConfig appConfig  = null;

    private String serverHostname   = null;
    private int serverPort          = -1;
    
    private final String REGISTER_PAYLOAD           = "{\"clientGUID\":\"%s\",\"port\":\"%s\",\"numberFilesShared\":\"%s\"}";
    private final String HEARTBEAT_PAYLOAD          = "{\"clientGUID\":\"%s\",\"numberFilesShared\":\"%s\"}";
    private final String GET_CONN_DETAILS_PAYLOAD   = "{\"clientGUID\":\"%s\"}";
    /*
        /register
        /getServerConnectionInfo
        /heartbeat
    */
    
    public ServerConsumer() throws Exception{
        
        appConfig = new AppConfig();
        
        this.serverHostname = appConfig.getProperty("lift.server.hostname");
        this.serverPort     = Integer.parseInt(appConfig.getProperty("lift.server.port", "-1"));
                
        logger.info(String.format("Server connection details configured: [%s:%s]", serverHostname, Integer.toString(serverPort)));
        
        // Fail if vital parameters are not set.
        
        if(this.serverPort == -1){
            throw new Exception("Could not determine server port");
        }
        
        if(this.serverHostname == null){
            throw new Exception("Could not determine server port");
        }        
    }
    
    

    private String getServerUrl(){
        return this.serverHostname + ":" + Integer.toString(this.serverPort);
    }
    
    public boolean register(String clientId, int port, int numberFilesShared){
        
        /* Payload
            {
                "clientGUID"			: "testuser",
                "port"					: "45633",
                "numberFilesShared"		: "1"
            }
        */
        
        String registerEndpoint = getServerUrl() + "/register";
        JSONObject payloadObj   = new JSONObject();
        payloadObj.put("clientGUID", clientId);
        payloadObj.put("port", Integer.toString(port));
        payloadObj.put("numberFilesShared", Integer.toString(numberFilesShared));

        logger.info(String.format("Attempting server request POST to server: \nEndpoint:\n%s\nPayload:\n%s\n\n", registerEndpoint, payloadObj.toString()));
        
        try{
            Client client  = ClientBuilder.newClient();     
        
            Response response = client.target(registerEndpoint)
                                  .request(MediaType.APPLICATION_JSON)
                                  .post(Entity.entity(payloadObj.toString(), MediaType.APPLICATION_JSON));            
            
            logger.info("Response status: " + response.getStatus());
            
            String output = response.readEntity(String.class);
            JSONObject jsonObject = new JSONObject(output);
            logger.info("Response output \n" + jsonObject.toString(2));
            
            if(response.getStatus() == 200){

                if(jsonObject.has("status")){
                    
                    String status = (String) jsonObject.get("status");
                    
                    boolean wasClientRegistered = (null != status 
                                                    && !status.isEmpty()
                                                    && status.equalsIgnoreCase("success"));
                    
                    logger.info("was client registed? " + wasClientRegistered);
                    return wasClientRegistered;
                }
                
            } else{
                
                logger.error("Unable to receive OK signal from server.");
            }
            
        } catch(Exception ex){
            logger.error("Failed to to operation on server.");
            ex.printStackTrace();
            logger.error(ex.toString());
        }
        
        
        return false;
    }
    
    
    
    public Map<String,String> getConnectionDetails(String clientId){
        Map<String,String> map = new HashMap<String,String>();
        
        /* Payload
            {
                "clientGUID"			: "testuser",
            }
        */        
        
        String registerEndpoint = getServerUrl()  + "/getServerConnectionInfo";
        JSONObject payloadObj   = new JSONObject();
        payloadObj.put("clientGUID", clientId);
        
        logger.info(String.format("Attempting server request POST to server: \nEndpoint:\n%s\nPayload:\n%s\n\n", registerEndpoint, payloadObj.toString()));
        
        try{
            Client client  = ClientBuilder.newClient();     
        
            Response response = client.target(registerEndpoint)
                                  .request(MediaType.APPLICATION_JSON)
                                  .post(Entity.entity(payloadObj.toString(), MediaType.APPLICATION_JSON));            
            
            logger.info("Response status: " + response.getStatus());
            
            String output = response.readEntity(String.class);
            JSONObject jsonObject = new JSONObject(output);
            logger.info("Response output \n" + jsonObject.toString(2));
            
            if(response.getStatus() == 200){

                if(jsonObject.has("status")){
                    
                    String status = (String) jsonObject.get("status");
                    
                    boolean connectionDetailsRetrieved = (null != status 
                                                            && !status.isEmpty()
                                                            && status.equalsIgnoreCase("success"));
                    
                    logger.info("Were connection details retrieved? " + connectionDetailsRetrieved);
                    
                    if(connectionDetailsRetrieved){
                        
                        JSONObject connectionDetailsJson = jsonObject.getJSONObject("message");
                        
                        if(connectionDetailsJson.has("ip")){
                            map.put("ip", (String)connectionDetailsJson.get("ip"));
                        }

                        if(connectionDetailsJson.has("port")){
                            map.put("port", (String)connectionDetailsJson.get("port"));
                        }                        
                    }
                }
                
            } else{
                
                logger.error("Unable to receive OK signal from server.");
            }
            
        } catch(Exception ex){
            logger.error("Failed to to operation on server.");
            ex.printStackTrace();
            logger.error(ex.toString());
        }
        
        logger.info("Connection details retrieved: ");
        
        for(String key : map.keySet()){
            logger.info(String.format("%s -> %s", key, map.get(key)));
        }
        
        return map;
    }

    public boolean sendHeartBeat(String clientId, int numberFilesShared){
        /* Payload
            {
                "clientGUID"			: "testuser",
                "numberFilesShared"		: "1"
            }
        */        
        
        String registerEndpoint = getServerUrl()  + "/heartbeat";
        
        JSONObject payloadObj   = new JSONObject();
        payloadObj.put("clientGUID", clientId);
        payloadObj.put("numberFilesShared", Integer.toString(numberFilesShared));
        
        logger.info(String.format("Attempting server request POST to server: \nEndpoint:\n%s\nPayload:\n%s\n\n", registerEndpoint, payloadObj.toString()));
        
        try{
            Client client  = ClientBuilder.newClient();     
        
            Response response = client.target(registerEndpoint)
                                  .request(MediaType.APPLICATION_JSON)
                                  .post(Entity.entity(payloadObj.toString(), MediaType.APPLICATION_JSON));            
            
            logger.info("Response status: " + response.getStatus());
            
            String output = response.readEntity(String.class);
            JSONObject jsonObject = new JSONObject(output);
            logger.info("Response output \n" + jsonObject.toString(2));
            
            if(response.getStatus() == 200){

                if(jsonObject.has("status")){
                    
                    String status = (String) jsonObject.get("status");
                    
                    boolean wasHeartBeatSent = (null != status 
                                                    && !status.isEmpty()
                                                    && status.equalsIgnoreCase("success"));
                    
                    logger.info("was heartbeat sent? " + wasHeartBeatSent);
                    return wasHeartBeatSent;
                }
                
            } else{
                
                logger.error("Unable to receive OK signal from server.");
            }
            
        } catch(Exception ex){
            logger.error("Failed to to operation on server.");
            ex.printStackTrace();
            logger.error(ex.toString());
        }
        
        
        return false;
    }
    
    
    public static void main(String[] args) {
        
        try{
            ServerConsumer serverConsumer = new ServerConsumer();
            serverConsumer.sendHeartBeat("testuser", 40);
            
            
            Map<String, String> configInformation = serverConsumer.getConnectionDetails("testuser");
            
            for(String key : configInformation.keySet()){
                System.out.println(String.format("%s -> %s", key, configInformation.get(key)));
            }
            
            
            boolean registered = serverConsumer.register("mahfoobah", 3244, 1);
            
            System.out.println("Maafoobah registered? " + registered);
            
        } catch(Exception ex){
            ex.printStackTrace();
        }
        
    }    
}
