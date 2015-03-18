package sri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class FileDictionary {

    private static FileDictionary instance = null;
    final private ArrayList<IndexedFile> fileIDs; // File Dictionary ID -> file
    final private Map<String, Integer> files; // File Dictionary file -> ID

    private FileDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedFile> _fileIDs = null;
        Map<String, Integer> _files = null;

        File dirDictionary = new File(configReader.getStringDirDictionary());
        dirDictionary.mkdir();
        File serDictionary = new File(configReader.getStringDirDictionary() + "fileDictionary.ser");
        if (serDictionary.canRead()) {
            try {
                FileInputStream fis = new FileInputStream(serDictionary);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    _fileIDs = (ArrayList<IndexedFile>) ois.readObject();
                    _files = new HashMap(300);

                    Iterator<IndexedFile> it = _fileIDs.iterator();
                    while (it.hasNext()) {
                        IndexedFile iW = it.next();
                        _files.put(iW.getFile(), iW.getID());
                    }
                    IndexedFile.setNextID(_fileIDs.size());
                }
            } catch (Exception e) {
                System.out.println("FileDictionary failed loading its serialized file.");
            }
        }

        if (_fileIDs == null) {
            fileIDs = new ArrayList(200);
        } else {
            fileIDs = _fileIDs;
        }

        if (_files == null) {
            files = new HashMap(300);
        } else {
            files = _files;
        }
    }

    protected static FileDictionary getInstance() {
        if (instance == null) {
            instance = new FileDictionary();
        }
        return instance;
    }

    protected ArrayList<IndexedFile> accessDictionary() {
        return fileIDs;
    }

    protected Integer search(String file) {
        return files.get(file);
    }

    protected IndexedFile search(Integer idFile) {
        if (idFile < fileIDs.size()) {
            return fileIDs.get(idFile);
        } else {
            return null;
        }
    }

    protected Integer add(IndexedFile newFile) {
        Integer idFile = newFile.getID();
        files.put(newFile.getFile(), idFile);
        fileIDs.add(idFile, newFile);
        return idFile;
    }

    protected void saveDictionary(String stringDirDictionary) {
        try {
            File dirDictionary = new File(stringDirDictionary);
            dirDictionary.mkdir();
            FileOutputStream fos = new FileOutputStream(stringDirDictionary + "fileDictionary.ser");
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(fileIDs);
            }
        } catch (Exception e) {
            System.out.println("Failed serializing file dictionary.");
        }
    }

    protected int size() {
        return fileIDs.size();
    }

    protected Iterator<IndexedFile> iterator() {
        return fileIDs.iterator();
    }

}
