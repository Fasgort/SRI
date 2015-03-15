package sri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class WordDictionary {

    static private WordDictionary instance = null;
    final private ArrayList<String> wordIDs; // File Dictionary ID -> word
    final private Map<String, Integer> words; // File Dictionary word -> ID

    protected WordDictionary() {
        wordIDs = new ArrayList(200);
        words = new HashMap(300);
    }

    public static WordDictionary getInstance() {
        if (instance == null) {
            instance = new WordDictionary();
        }
        return instance;
    }

    public Integer search(String word) {
        return words.get(word);
    }

    public String search(Integer idWord) {
        if (idWord < wordIDs.size()) {
            return wordIDs.get(idWord);
        } else {
            return null;
        }
    }

    public Integer add(String word) {
        IndexedWord newWord = new IndexedWord(word);
        Integer idWord = newWord.getID();
        words.put(word, idWord);
        wordIDs.add(idWord, word);
        return idWord;
    }

}
