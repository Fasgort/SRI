package sri;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Fasgort
 */
public class FileData implements Comparable<FileData> {

    final private IndexedFile file;
    private int count;
    final private Map<Integer, WordFrequency> wordFrequency;

    public FileData(IndexedFile _file) {
        file = _file;
        count = 0;
        wordFrequency = new HashMap(500);
    }

    public WordFrequency search(IndexedWord word) {
        return wordFrequency.get(word.getID());
    }

    public void add(IndexedWord word) {
        wordFrequency.put(word.getID(), new WordFrequency(word));
        count++;
    }

    public void updateMaxFrequentWord() {
        Iterator<WordFrequency> it = wordFrequency.values().iterator();
        int maxCount = -1;
        while (it.hasNext()) {
            WordFrequency wf = it.next();
            if (wf.getCount() > maxCount) {
                maxCount = wf.getCount();
            }
        }
        file.setMaxFrequentWord(maxCount);
    }

    public String getFile() {
        return file.getFile();
    }

    public Integer getID() {
        return file.getID();
    }

    public void addCount() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public int size() {
        return wordFrequency.size();
    }
    
    @Override
    public int compareTo(FileData w) {
        return w.count - count;
    }

}
