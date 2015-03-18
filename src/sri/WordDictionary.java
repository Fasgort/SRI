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
public class WordDictionary {

    private static WordDictionary instance = null;
    final private ArrayList<IndexedWord> wordIDs; // File Dictionary ID -> word
    final private Map<String, Integer> words; // File Dictionary word -> ID

    private WordDictionary() {
        ConfigReader configReader = ConfigReader.getInstance();

        ArrayList<IndexedWord> _wordIDs = null;
        Map<String, Integer> _words = null;

        File dirDictionary = new File(configReader.getStringDirDictionary());
        dirDictionary.mkdir();
        File serDictionary = new File(configReader.getStringDirDictionary() + "wordDictionary.ser");
        if (serDictionary.canRead()) {
            try {
                FileInputStream fis = new FileInputStream(serDictionary);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    _wordIDs = (ArrayList<IndexedWord>) ois.readObject();
                    _words = new HashMap(300);

                    Iterator<IndexedWord> it = _wordIDs.iterator();
                    while (it.hasNext()) {
                        IndexedWord iW = it.next();
                        _words.put(iW.getWord(), iW.getID());
                    }
                    IndexedWord.setNextID(_wordIDs.size());
                }
            } catch (Exception e) {
                System.out.println("WordDictionary failed loading its serialized file.");
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
        int idWord = newWord.getID();
        words.put(newWord.getWord(), idWord);
        wordIDs.add(idWord, newWord);
        return idWord;
    }

    protected int size() {
        return wordIDs.size();
    }

    public Iterator<IndexedWord> iterator() {
        return wordIDs.iterator();
    }

    protected void saveDictionary(String stringDirDictionary) {
        try {
            File dirDictionary = new File(stringDirDictionary);
            dirDictionary.mkdir();
            FileOutputStream fos = new FileOutputStream(stringDirDictionary + "wordDictionary.ser");
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(wordIDs);
            }
        } catch (Exception e) {
            System.out.println("Failed serializing word dictionary.");
        }
    }

}
