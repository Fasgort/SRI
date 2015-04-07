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
public class WordDictionary {

    private static volatile WordDictionary instance = null;
    final private ArrayList<IndexedWord> wordIDs; // File Dictionary ID -> word
    final private Map<String, Integer> words; // File Dictionary word -> ID
    private transient BitSet exists;
    private transient int bitsetSize;
    private transient boolean dirty = false;

    private WordDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedWord> _wordIDs = null;
        Map<String, Integer> _words = null;

        File serDictionary = new File(configReader.getStringDirIndex() + configReader.getStringWordDictionary());
        if (serDictionary.canRead()) {
            try (FileInputStream fis = new FileInputStream(serDictionary);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                _wordIDs = (ArrayList<IndexedWord>) ois.readObject();
                _words = new HashMap(300);

                Iterator<IndexedWord> it = _wordIDs.iterator();
                while (it.hasNext()) {
                    IndexedWord iW = it.next();
                    _words.put(iW.getWord(), iW.getID());
                }
                IndexedWord.setNextID(_wordIDs.size());
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex);
            }
        }

        if (_wordIDs == null) {
            wordIDs = new ArrayList(200);
        } else {
            wordIDs = _wordIDs;
        }

        if (_words == null) {
            words = new HashMap(300);
        } else {
            words = _words;
        }

        exists = new BitSet(wordIDs.size());
        bitsetSize = wordIDs.size();

    }

    protected static WordDictionary getInstance() {
        if (instance == null) {
            instance = new WordDictionary();
        }
        return instance;
    }

    protected ArrayList<IndexedWord> accessDictionary() {
        return wordIDs;
    }

    protected int search(String word) {
        Integer idWord = words.get(word);
        if (idWord == null) {
            return -1;
        }
        return idWord;
    }

    protected IndexedWord search(int idWord) {
        if (idWord < wordIDs.size()) {
            return wordIDs.get(idWord);
        } else {
            return null;
        }
    }

    protected int add(IndexedWord newWord) {
        dirty = true;
        int idWord = newWord.getID();
        words.put(newWord.getWord(), idWord);
        wordIDs.add(idWord, newWord);
        return idWord;
    }

    protected void move(int oldID, int newID) {
        dirty = true;
        IndexedWord movedWord = wordIDs.get(oldID);
        IndexedWord deletedWord = wordIDs.get(newID);

        wordIDs.remove(newID);
        wordIDs.add(newID, movedWord);

        wordIDs.remove(oldID);
        wordIDs.add(oldID, deletedWord);

        words.replace(movedWord.getWord(), newID);
        words.replace(deletedWord.getWord(), oldID);

        movedWord.setID(newID);
        deletedWord.setID(oldID);
    }

    public void doesExist(int idWord) {
        if (idWord <= bitsetSize) {
            exists.set(idWord);
        }
    }

    public void doesNotExist(int idWord) {
        if (idWord <= bitsetSize) {
            exists.clear(idWord);
        }
    }

    protected boolean exists(int idWord) {
        if (idWord <= bitsetSize) {
            return exists.get(idWord);
        } else {
            return true;
        }
    }

    protected int size() {
        return wordIDs.size();
    }

    public Iterator<IndexedWord> iterator() {
        return wordIDs.iterator();
    }

    protected void setDirty() {
        dirty = true;
    }

    protected void cleanDictionary() {
        int wordID;
        while ((wordID = exists.previousClearBit(wordIDs.size() - 1)) != -1) {
            String deleted = wordIDs.get(wordID).getWord();
            words.remove(deleted);
            wordIDs.remove(wordID);
        }
    }

    protected void saveDictionary() {
        if (dirty) {
            ConfigReader configReader = ConfigReader.getInstance();

            File dirDictionary = new File(configReader.getStringDirIndex());
            dirDictionary.mkdir();
            try (FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringWordDictionary());
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(wordIDs);
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

    protected BitSet getExistBitset() {
        return exists;
    }

    protected void setExistBitset(BitSet bitset) {
        exists = bitset;
    }

    public int getBitsetSize() {
        return bitsetSize;
    }

    public void setBitsetSize(int size) {
        bitsetSize = size;
    }

}
