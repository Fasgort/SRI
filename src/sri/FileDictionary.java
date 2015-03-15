package sri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class FileDictionary {

    static private FileDictionary instance = null;
    final private ArrayList<String> fileIDs; // File Dictionary ID -> file
    final private Map<String, Integer> files; // File Dictionary file -> ID

    protected FileDictionary() {
        fileIDs = new ArrayList(200);
        files = new HashMap(300);
    }

    public static FileDictionary getInstance() {
        if (instance == null) {
            instance = new FileDictionary();
        }
        return instance;
    }

    public Integer search(String file) {
        return files.get(file);
    }

    public String search(Integer idFile) {
        if (idFile < fileIDs.size()) {
            return fileIDs.get(idFile);
        } else {
            return null;
        }
    }

    public Integer add(String file) {
        IndexedFile newFile = new IndexedFile(file);
        Integer idFile = newFile.getID();
        files.put(file, idFile);
        fileIDs.add(idFile, file);
        return idFile;
    }

}
