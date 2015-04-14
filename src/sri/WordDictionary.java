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
public class WordDictionary extends Dictionary<IndexedWord> {

    private static volatile WordDictionary instance = null;

    private WordDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedWord> wordIDs = null;
        Map<String, Integer> words = null;

        File serDictionary = new File(configReader.getStringDirIndex() + configReader.getStringWordDictionary());
        if (serDictionary.canRead()) {
            try (FileInputStream fis = new FileInputStream(serDictionary);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                wordIDs = (ArrayList<IndexedWord>) ois.readObject();
                words = new HashMap(300);

                Iterator<IndexedWord> it = wordIDs.iterator();
                while (it.hasNext()) {
                    IndexedWord iW = it.next();
                    words.put(iW.getWord(), iW.getID());
                }
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
            entries = new HashMap(300);
        } else {
            entries = words;
        }

        exists = new BitSet(entryIDs.size());
        bitsetSize = entryIDs.size();

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
    protected void move(int oldID, int newID) {
        dirty = true;
        IndexedWord movedWord = entryIDs.get(oldID);
        IndexedWord deletedWord = entryIDs.get(newID);

        entryIDs.remove(newID);
        entryIDs.add(newID, movedWord);

        entryIDs.remove(oldID);
        entryIDs.add(oldID, deletedWord);

        entries.replace(movedWord.getWord(), newID);
        entries.replace(deletedWord.getWord(), oldID);

        movedWord.setID(newID);
        deletedWord.setID(oldID);
    }

    @Override
    protected void cleanDictionary() {
        int wordID;
        while ((wordID = exists.previousClearBit(entryIDs.size() - 1)) != -1) {
            String deleted = entryIDs.get(wordID).getWord();
            entries.remove(deleted);
            entryIDs.remove(wordID);
        }
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
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

}
