package sri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
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
    transient private BitSet existence;
    transient private int existence_end;
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

        existence = new BitSet(fileIDs.size());
        existence_end = fileIDs.size();

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

    protected void move(int oldID, int newID) {
        dirty = true;
        IndexedFile movedFile = fileIDs.get(oldID);
        IndexedFile deletedFile = fileIDs.get(newID);

        fileIDs.remove(newID);
        fileIDs.add(newID, movedFile);

        fileIDs.remove(oldID);
        fileIDs.add(oldID, deletedFile);

        files.replace(movedFile.getFile(), newID);
        files.replace(deletedFile.getFile(), oldID);

        movedFile.setID(newID);
        deletedFile.setID(oldID);
    }

    protected void delete(int fileID) {
        String deleted = fileIDs.get(fileID).getFile();
        files.remove(deleted);
        fileIDs.remove(fileID);
    }

    public void doesExist(int idFile) {
        if (idFile <= existence_end) {
            existence.set(idFile);
        }
    }

    public void doesNotExist(int idFile) {
        if (idFile <= existence_end) {
            existence.clear(idFile);
        }
    }

    protected boolean exists(int idFile) {
        if (idFile <= existence_end) {
            return existence.get(idFile);
        } else {
            return true;
        }
    }

    protected void setDirty() {
        dirty = true;
    }

    protected void cleanDictionary() {
        int fileID;
        while ((fileID = existence.previousClearBit(fileIDs.size() - 1)) != -1) {
            delete(fileID);
        }
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

    protected BitSet getExistence() {
        return existence;
    }

    protected void setExistence(BitSet _existence) {
        existence = _existence;
    }

    public int getExistence_end() {
        return existence_end;
    }

    public void setExistence_end(int _existence_end) {
        existence_end = _existence_end;
    }

    protected int size() {
        return fileIDs.size();
    }

    protected int existingDocuments() {
        return existence.cardinality();
    }

    protected Iterator<IndexedFile> iterator() {
        return fileIDs.iterator();
    }

}
