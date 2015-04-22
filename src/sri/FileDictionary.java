package sri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Fasgort
 */
public class FileDictionary extends Dictionary<IndexedFile, Long> {

    private static volatile FileDictionary instance = null;

    private FileDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedFile> fileIDs = null;
        ConcurrentMap<Long, Integer> checksums = null;

        File serDictionary = new File(configReader.getStringDirIndex() + configReader.getStringFileDictionary());
        if (serDictionary.canRead()) {
            try (FileInputStream fis = new FileInputStream(serDictionary);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                fileIDs = (ArrayList<IndexedFile>) ois.readObject();
                checksums = (ConcurrentMap<Long, Integer>) ois.readObject();
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

        if (checksums == null) {
            entries = new ConcurrentHashMap(300);
        } else {
            entries = checksums;
        }

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
        entries.put(newFile.getChecksum(), idFile);
        entryIDs.add(idFile, newFile);
        return idFile;
    }

    @Override
    protected void replaceAndDelete(int movedFileID, int deletedFileID) {
        dirty = true;
        IndexedFile movedFile = entryIDs.get(movedFileID);
        IndexedFile deletedFile = entryIDs.get(deletedFileID);

        entryIDs.set(deletedFileID, movedFile);
        entryIDs.remove(movedFileID);

        entries.replace(movedFile.getChecksum(), deletedFileID);
        entries.remove(deletedFile.getChecksum());

        movedFile.setID(deletedFileID);

        // Clean old files
        ConfigReader configReader = ConfigReader.getInstance();

        File deleted;
        deleted = new File(configReader.getStringDirColEnN() + deletedFile.getFile().replace(".html", ".txt"));
        deleted.deleteOnExit();
        deleted = new File(configReader.getStringDirColEnStop() + deletedFile.getFile().replace(".html", ".txt"));
        deleted.deleteOnExit();
        deleted = new File(configReader.getStringDirColEnStem() + deletedFile.getFile().replace(".html", ".txt"));
        deleted.deleteOnExit();
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
                oos.writeObject(entries);
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

    @Override
    protected void doesExist(int idFile) {
        entryIDs.get(idFile).doesExist();
    }

    @Override
    protected void doesNotExist(int idFile) {
        entryIDs.get(idFile).doesNotExist();
    }

    @Override
    protected boolean exists(int idFile) {
        return entryIDs.get(idFile).exists();
    }

    protected boolean isNew(int idFile) {
        return entryIDs.get(idFile).isNew();
    }

    protected boolean isNamed(int idFile) {
        return entryIDs.get(idFile).isNamed();
    }

}
