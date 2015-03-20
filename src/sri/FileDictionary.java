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
    transient private boolean dirty = false;

    private FileDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedFile> _fileIDs = null;
        Map<String, Integer> _files = null;

        File serDictionary = new File(configReader.getStringDirIndex() + configReader.getStringFileDictionary());
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

    protected int search(String file) {
        Integer idWord = files.get(file);
        if (idWord == null) {
            return -1;
        }
        return idWord;
    }

    protected IndexedFile search(int idFile) {
        if (idFile < fileIDs.size()) {
            return fileIDs.get(idFile);
        } else {
            return null;
        }
    }

    protected int add(IndexedFile newFile) {
        dirty = true;
        int idFile = newFile.getID();
        files.put(newFile.getFile(), idFile);
        fileIDs.add(idFile, newFile);
        return idFile;
    }

    protected void setDirty() {
        dirty = true;
    }

    protected void saveDictionary() {
        if (dirty) {
            ConfigReader configReader = ConfigReader.getInstance();

            try {
                File dirDictionary = new File(configReader.getStringDirIndex());
                dirDictionary.mkdir();
                FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringFileDictionary());
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(fileIDs);
                }
            } catch (Exception e) {
                System.out.println("Failed serializing file dictionary.");
            }
        }
    }

    protected int size() {
        int count = 0;
        Iterator<IndexedFile> itf = fileIDs.iterator();

        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            if (iF.exists()) {
                count++;
            }
        }

        return count;
    }

    protected Iterator<IndexedFile> iterator() {
        return fileIDs.iterator();
    }

}
