package sri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class FileDictionary extends Dictionary<IndexedFile> {

    private static volatile FileDictionary instance = null;
    private transient BitSet modified;

    private FileDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedFile> fileIDs = null;
        Map<String, Integer> files = null;

        File serDictionary = new File(configReader.getStringDirIndex() + configReader.getStringFileDictionary());
        if (serDictionary.canRead()) {
            try (FileInputStream fis = new FileInputStream(serDictionary);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                fileIDs = (ArrayList<IndexedFile>) ois.readObject();
                files = new HashMap(300);

                Iterator<IndexedFile> it = fileIDs.iterator();
                while (it.hasNext()) {
                    IndexedFile iW = it.next();
                    files.put(iW.getFile(), iW.getID());
                }
                IndexedFile.setNextID(fileIDs.size());
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex);
            }
        }

        if (fileIDs == null) {
            entryIDs = new ArrayList(200);
        } else {
            entryIDs = fileIDs;
        }

        if (files == null) {
            entries = new HashMap(300);
        } else {
            entries = files;
        }

        exists = new BitSet(entryIDs.size());
        modified = new BitSet(entryIDs.size());
        bitsetSize = entryIDs.size();

    }

    protected static FileDictionary getInstance() {
        if (instance == null) {
            instance = new FileDictionary();
        }
        return instance;
    }

    @Override
    protected int add(IndexedFile newFile) {
        dirty = true;
        int idFile = newFile.getID();
        entries.put(newFile.getFile(), idFile);
        entryIDs.add(idFile, newFile);
        return idFile;
    }

    @Override
    protected void move(int oldID, int newID) {
        dirty = true;
        IndexedFile movedFile = entryIDs.get(oldID);
        IndexedFile deletedFile = entryIDs.get(newID);

        entryIDs.remove(newID);
        entryIDs.add(newID, movedFile);

        entryIDs.remove(oldID);
        entryIDs.add(oldID, deletedFile);

        entries.replace(movedFile.getFile(), newID);
        entries.replace(deletedFile.getFile(), oldID);

        movedFile.setID(newID);
        deletedFile.setID(oldID);
    }

    @Override
    protected void cleanDictionary() {
        int fileID;
        while ((fileID = exists.previousClearBit(entryIDs.size() - 1)) != -1) {
            String deleted = entryIDs.get(fileID).getFile();
            entries.remove(deleted);
            entryIDs.remove(fileID);
        }
    }

    @Override
    protected void saveDictionary() {
        if (dirty) {
            ConfigReader configReader = ConfigReader.getInstance();

            File dirDictionary = new File(configReader.getStringDirIndex());
            dirDictionary.mkdir();
            try (FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringFileDictionary());
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(entryIDs);
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

    protected void isModified(int idFile) {
        if (idFile <= bitsetSize) {
            modified.set(idFile);
        }
    }

    protected void isNotModified(int idFile) {
        if (idFile <= bitsetSize) {
            modified.clear(idFile);
        }
    }

    protected boolean modified(int idFile) {
        if (idFile <= bitsetSize) {
            return modified.get(idFile);
        } else {
            return true;
        }
    }

    protected BitSet getModifiedBitset() {
        return modified;
    }

    protected void setModifiedBitset(BitSet bitset) {
        modified = bitset;
    }

}
