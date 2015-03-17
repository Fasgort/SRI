package sri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class FileDictionary {

    static private FileDictionary instance = null;
    final private ArrayList<IndexedFile> fileIDs; // File Dictionary ID -> file
    final private Map<String, Integer> files; // File Dictionary file -> ID

    protected FileDictionary() {
        fileIDs = new ArrayList(300);
        files = new HashMap(500);
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

    public IndexedFile search(Integer idFile) {
        if (idFile < fileIDs.size()) {
            return fileIDs.get(idFile);
        } else {
            return null;
        }
    }

    public Integer add(IndexedFile newFile) {
        Integer idFile = newFile.getID();
        files.put(newFile.getFile(), idFile);
        fileIDs.add(idFile, newFile);
        return idFile;
    }

    public int size() {
        return fileIDs.size();
    }

    public Iterator<IndexedFile> iterator() {
        return fileIDs.iterator();
    }

}
