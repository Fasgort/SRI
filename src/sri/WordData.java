package sri;

import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;
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

    protected WordData(IndexedWord _word) {
        word = _word;
        count = 0;
        fileFrequency = new HashMap(10000);
    }

    protected FileFrequency search(IndexedFile file) {
        return fileFrequency.get(file.getID());
    }

    protected void add(IndexedFile file) {
        fileFrequency.put(file.getID(), new FileFrequency(file));
        word.addDocument();
        count++;
    }

    protected void generateIDF(int numberDocuments) {
        word.generateIDF(numberDocuments);
    }

    protected void generateWeight() {
        Collection<FileFrequency> collect = fileFrequency.values();
        Iterator<FileFrequency> it = collect.iterator();

        double idf = word.getIDF();
        double weightSum = 0;
        FileFrequency ff;

        while (it.hasNext()) {
            ff = it.next();
            ff.generateFrequency();
            ff.generateWeight(idf);
            weightSum += pow(ff.getWeight(), 2);
        }

        weightSum = sqrt(weightSum);

        it = collect.iterator();
        while (it.hasNext()) {
            it.next().generateNormalizedWeight(weightSum);
        }
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

    @Override
    public int compareTo(WordData w) {
        return w.count - count;
    }

}
