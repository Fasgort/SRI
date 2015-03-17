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
            IndexedWord iW = new IndexedWord(word);
            idWord = wordDictionary.add(iW);
            wordFrequency.add(idWord, new WordData(iW));
        }
        return idWord;
    }
    
    public IndexedWord searchWord(Integer idWord) {
        return wordDictionary.search(idWord);
    }
    
    public Integer searchFile(String file) {
        Integer idFile = fileDictionary.search(file);
        if (idFile == null) {
            return fileDictionary.add(new IndexedFile(file));
        }
        return idFile;
    }
    
    public IndexedFile searchFile(Integer idFile) {
        return fileDictionary.search(idFile);
    }
    
    public boolean checksumFile(Integer idFile, long checksum) {
        return fileDictionary.search(idFile).getChecksum() == checksum;
    }
    
    public void updateChecksumFile(Integer idFile, long checksum) {
        fileDictionary.search(idFile).setChecksum(checksum);
    }
    
    public void addFrequency(Integer idWord, Integer idFile) {
        WordData wd = wordFrequency.get(idWord);
        IndexedFile iF = fileDictionary.search(idFile);
        FileFrequency ff = wd.search(iF);
        if (ff == null) {
            wd.add(iF);
        } else {
            ff.addCount();
            wd.addCount();
        }
    }
    
    public void generateIndex() {
        Iterator<WordData> itw = wordFrequency.iterator();
        
        while (itw.hasNext()) {
            WordData wd = itw.next();
            wd.generateIDF(fileDictionary.size());
            wd.generateWeight();
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
                    Iterator<WordData> listIterator = list.descendingIterator();
                    int index = list.size();
                    boolean added = false;
                    while (listIterator.hasNext()) {
                        WordData word2 = listIterator.next();
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
