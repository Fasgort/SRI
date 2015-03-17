package sri;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class WordData implements Comparable<WordData> {

    final private IndexedWord word;
    private int count;
    final private Map<Integer, FileFrequency> fileFrequency;

    protected WordData(IndexedWord _word) {
        word = _word;
        count = 0;
        fileFrequency = new HashMap(7500);
    }

    protected FileFrequency search(IndexedFile file) {
        return fileFrequency.get(file.getID());
    }

    protected void add(IndexedFile file) {
        fileFrequency.put(file.getID(), new FileFrequency(file));
        count++;
    }

    protected String getWord() {
        return word.getWord();
    }

    protected Integer getID() {
        return word.getID();
    }

    protected void addCount() {
        count++;
    }

    protected int getCount() {
        return count;
    }

    protected int size() {
        return fileFrequency.size();
    }

    @Override
    public int compareTo(WordData w) {
        return w.count - count;
    }

}
