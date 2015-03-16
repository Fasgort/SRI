package sri;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class WordData implements Comparable<WordData> {

    final private Integer idWord;
    private int count;
    final private Map<Integer, FileFrequency> fileFrequency;

    public WordData(Integer id) {
        idWord = id;
        count = 0;
        fileFrequency = new HashMap(10000);
    }

    public FileFrequency search(Integer idFile) {
        return fileFrequency.get(idFile);
    }

    public void add(Integer idFile) {
        fileFrequency.put(idFile, new FileFrequency(idFile));
        count++;
    }

    public Integer getID() {
        return idWord;
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
