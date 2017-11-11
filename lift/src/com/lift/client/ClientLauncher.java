package com.lift.client;

import com.lift.common.Operation;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class handles the Lift client operations as the end user will use.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ClientLauncher {
    
    private static final Map<String, String> commandDescriptions = new LinkedHashMap<>();
    private static final Map<String, String> optionDescriptions  = new LinkedHashMap<>();
    
    
    public static void initOptions() {
        
        commandDescriptions.put(Operation.ADD,      "Add a file to local repository");
        commandDescriptions.put(Operation.FILES,    "List files");
        commandDescriptions.put(Operation.GET,      "Get the file from remote repository");
        commandDescriptions.put(Operation.ID,       "Show the user GUID in the network");
        commandDescriptions.put(Operation.RM,       "Remove a file from local repository");
        commandDescriptions.put(Operation.SHARE,    "Share a file and generate it's UFL");
        commandDescriptions.put(Operation.UFL,      "Generate the file's UFL");
        commandDescriptions.put(Operation.VERSION,  "Show the Lift version information");
        
        optionDescriptions.put(Operation.HELP,      "Print usage");
        
    }

    public static void main(String[] args) {
        
        initOptions();
        
        ClientManager client = new ClientManager();
        // 
        // Parser
        //
        loop: for (String opt : args) {
        
            switch(opt) {
                
                case Operation.SHARE:
                    checkOptArgsNumber(Operation.SHARE, args, 2);
                    client.share(args[1]);
                    break loop;
                
                case Operation.FILES:
                    client.files();
                    break loop;
                
                case Operation.RM:
                    checkOptArgsNumber(Operation.RM, args, 2);
                    client.rm(args[1]);
                    break loop;
                    
                case Operation.ADD:
                    checkOptArgsNumber(Operation.ADD, args, 2);
                    client.add(args[1]);
                    break loop;
                
                case Operation.ID:
                    client.id();
                    break loop;
                
                case Operation.GET:
                    checkOptArgsNumber(Operation.GET, args, 2);
                    client.get(args[1]);
                    break loop;
                    
                case Operation.UFL:
                    checkOptArgsNumber(Operation.UFL, args, 2);
                    client.ufl(args[1]);
                    break loop;
                
                case Operation.VERSION:
                    client.version();
                    break loop;
                
                case Operation.HELP:
                    showUsage();
                    break loop;
                
                default:
                    System.out.println("lift: '" + opt + "' is not a lift command.");
                    System.out.println("See 'lift --help'");
                    break loop;
            
            }
        }
    }
    
    private static void showUsage() {
        System.out.printf("\nUsage:  lift COMMAND\n\n");
        System.out.printf("A P2P client for lightweight file transfer\n\n");
        
        System.out.printf("Options:\n\n");
        
        optionDescriptions.keySet().forEach((option) -> {
            System.out.printf("  %-10s%s\n", option, optionDescriptions.get(option));
        });
        
        System.out.printf("\n");
        
        System.out.printf("Commands:\n\n");
        
        commandDescriptions.keySet().forEach((command) -> {
            System.out.printf("  %-10s%s\n", command, commandDescriptions.get(command));
        });
        
        System.out.printf("\n");
    }
    
    private static void checkOptArgsNumber(String opt, String[] args, int requiredArgsNumber) {
        if (args.length != requiredArgsNumber) {
            System.out.println("'lift " + opt + "' requires exactly " + (requiredArgsNumber - 1) + " argument(s)");
            System.out.println("See 'lift --help'");
            System.exit(0);
        }
    }
}
