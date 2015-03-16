package sri;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class WordData implements Comparable<WordData> {
    
    final private IndexedWord word;
    private int count;
    final private Map<Integer, FileFrequency> fileFrequency;
    
    public WordData(IndexedWord _word) {
        word = _word;
        count = 0;
        fileFrequency = new HashMap(10000);
    }
    
    public FileFrequency search(IndexedFile file) {
        return fileFrequency.get(file.getID());
    }
    
    public void add(IndexedFile file) {
        fileFrequency.put(file.getID(), new FileFrequency(file));
        count++;
    }
    
    public void generateFrequency() {
        Collection<FileFrequency> collect = fileFrequency.values();
        Iterator<FileFrequency> it = collect.iterator();
        
        while (it.hasNext()) {
            it.next().generateFrequency();
        }
    }
    
    public String getWord() {
        return word.getWord();
    }
    
    public Integer getID() {
        return word.getID();
    }
    
    public void addCount() {
        count++;
    }
    
    public int getCount() {
        return count;
    }
    
    @Override
    public int compareTo(WordData w) {
        return w.count - count;
    }
    
}
