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
public class WordDictionary extends Dictionary<IndexedWord, String> {

    private static volatile WordDictionary instance = null;

    private WordDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedWord> wordIDs = null;
        ConcurrentMap<String, Integer> words = null;

        File serDictionary = new File(configReader.getStringDirIndex() + configReader.getStringWordDictionary());
        if (serDictionary.canRead()) {
            try (FileInputStream fis = new FileInputStream(serDictionary);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                wordIDs = (ArrayList<IndexedWord>) ois.readObject();
                words = (ConcurrentMap<String, Integer>) ois.readObject();
                IndexedWord.setNextID(wordIDs.size());
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex);
            }
        }

        if (wordIDs == null) {
            entryIDs = new ArrayList(200);
        } else {
            entryIDs = wordIDs;
        }

        if (words == null) {
            entries = new ConcurrentHashMap(300);
        } else {
            entries = words;
        }

    }

    protected static WordDictionary getInstance() {
        if (instance == null) {
            instance = new WordDictionary();
        }
        return instance;
    }

    @Override
    protected int add(IndexedWord newWord) {
        dirty = true;
        int idWord = newWord.getID();
        entries.put(newWord.getWord(), idWord);
        entryIDs.add(idWord, newWord);
        return idWord;
    }

    @Override
    protected void replaceAndDelete(int oldID, int newID) {
        dirty = true;
        IndexedWord movedFile = entryIDs.get(oldID);
        IndexedWord deletedFile = entryIDs.get(newID);

        entryIDs.set(newID, movedFile);
        entryIDs.remove(oldID);

        entries.replace(movedFile.getWord(), newID);
        entries.remove(deletedFile.getWord());

        movedFile.setID(newID);
    }

    @Override
    protected void saveDictionary() {
        if (dirty) {
            ConfigReader configReader = ConfigReader.getInstance();

            File dirDictionary = new File(configReader.getStringDirIndex());
            dirDictionary.mkdir();
            try (FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringWordDictionary());
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(entryIDs);
                oos.writeObject(entries);
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

    @Override
    protected void doesExist(int idWord) {
        entryIDs.get(idWord).doesExist();
    }

    @Override
    protected void doesNotExist(int idWord) {
        entryIDs.get(idWord).doesNotExist();
    }

    @Override
    protected boolean exists(int idWord) {
        return entryIDs.get(idWord).exists();
    }

}
