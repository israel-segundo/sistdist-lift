
import java.util.UUID;

/**
 * This class represents a file in Lift local repository
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class File {
    
    private String name;
    private int hits;
    private String fileID;
    private String dateAdded;
    private int size;
    
    public File(String name, String fileID, String dateAdded, int size) {
        this.name = name;
        this.hits = 0;
        this.fileID = generateFileID();
        this.dateAdded = dateAdded;
        this.size = 0;
    }
    
    private static String generateFileID() {
        return UUID.randomUUID().toString().substring(24, 36);
    }
}
