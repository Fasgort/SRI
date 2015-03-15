package sri;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    static private DataManager instance = null;
    final private FileDictionary fileDictionary;
    final private WordDictionary wordDictionary;
    ArrayList<WordData> wordFrequency;

    protected DataManager() {
        fileDictionary = FileDictionary.getInstance();
        wordDictionary = WordDictionary.getInstance();
        wordFrequency = new ArrayList(10000);
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public Integer searchWord(String word) {
        Integer idWord = wordDictionary.search(word);
        if (idWord == null) {
            idWord = wordDictionary.add(word);
            wordFrequency.add(idWord, new WordData(idWord));
        }
        return idWord;
    }

    public String searchWord(Integer idWord) {
        return wordDictionary.search(idWord);
    }

    public Integer searchFile(String file) {
        Integer idFile = fileDictionary.search(file);
        if (idFile == null) {
            return fileDictionary.add(file);
        }
        return idFile;
    }

    public String searchFile(Integer idFile) {
        return fileDictionary.search(idFile);
    }

    public void addFrequency(Integer idWord, Integer idFile) {
        WordData wd = wordFrequency.get(idWord);
        FileFrequency ff = wd.search(idFile);
        if (ff == null) {
            wd.add(idFile);
        } else {
            ff.addCount();
            wd.addCount();
        }
    }

    public LinkedList<WordData> topFrequentWords(int sizeList) {
        LinkedList<WordData> list = new LinkedList();
        Iterator<WordData> wordIterator = wordFrequency.iterator();

        int minFrequency = 0;
        while (wordIterator.hasNext()) {
            WordData word1 = wordIterator.next();
            if (word1.getCount() > minFrequency || list.size() < sizeList) {
                if (list.size() == 0) {
                    list.add(word1);
                } else {
                    Iterator<WordData> nsw = list.descendingIterator();
                    int index = list.size();
                    boolean added = false;
                    while (nsw.hasNext()) {
                        WordData word2 = nsw.next();
                        if (word1.getCount() <= word2.getCount()) {
                            list.add(index, word1);
                            added = true;
                            break;
                        } else {
                            index--;
                        }
                    }
                    if (!added) {
                        list.addFirst(word1);
                    }
                }
                if (list.size() > sizeList) {
                    minFrequency = list.getLast().getCount();
                    list.removeLast();
                }
            }
        }

        return list;

    }

    public Integer wordQuantity() {
        return wordFrequency.size();
    }

}
