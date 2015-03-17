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

    protected WordDictionary(String stringDirDictionary) {
        ArrayList<IndexedWord> _wordIDs = null;
        Map<String, Integer> _words = null;

        File dirDictionary = new File(stringDirDictionary);
        dirDictionary.mkdir();
        File serDictionary = new File(stringDirDictionary + "wordDictionary.ser");
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

    protected static WordDictionary getInstance(String stringDirDictionary) {
        if (instance == null) {
            instance = new WordDictionary(stringDirDictionary);
        }
        return instance;
    }

    protected ArrayList<IndexedWord> accessDictionary() {
        return wordIDs;
    }

    protected Integer search(String word) {
        return words.get(word);
    }

    protected IndexedWord search(Integer idWord) {
        if (idWord < wordIDs.size()) {
            return wordIDs.get(idWord);
        } else {
            return null;
        }
    }

    protected Integer add(IndexedWord newWord) {
        Integer idWord = newWord.getID();
        words.put(newWord.getWord(), idWord);
        wordIDs.add(idWord, newWord);
        return idWord;
    }

    protected int size() {
        return wordIDs.size();
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
