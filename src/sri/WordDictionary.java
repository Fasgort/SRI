package sri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class WordDictionary {

    private static WordDictionary instance = null;
    final private ArrayList<IndexedWord> wordIDs; // File Dictionary ID -> word
    final private Map<String, Integer> words; // File Dictionary word -> ID

    protected WordDictionary() {
        wordIDs = new ArrayList(200);
        words = new HashMap(300);
        // Cargar los IndexedWord serializados aquí.
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

    public IndexedWord search(Integer idWord) {
        if (idWord < wordIDs.size()) {
            return wordIDs.get(idWord);
        } else {
            return null;
        }
    }

    public Integer add(IndexedWord newWord) {
        Integer idWord = newWord.getID();
        words.put(newWord.getWord(), idWord);
        wordIDs.add(idWord, newWord);
        return idWord;
    }
    
    public int size(){
        return wordIDs.size();
    }

}
