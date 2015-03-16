package sri;

/**
 *
 * @author Fasgort
 */
public class WordFrequency implements Comparable<WordFrequency> {

    final private IndexedWord word;
    private int count;

    public WordFrequency(IndexedWord _word) {
        word = _word;
        count = 1;
    }

    public void addCount() {
        count++;
    }

    public IndexedWord getWord() {
        return word;
    }

    public Integer getID() {
        return word.getID();
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(WordFrequency w) {
        return w.count - count;
    }

}
