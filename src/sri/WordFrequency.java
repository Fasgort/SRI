package sri;

/**
 *
 * @author Fasgort
 */
public class WordFrequency implements Comparable<WordFrequency> {

    final private IndexedWord word;
    private int count;

    protected WordFrequency(IndexedWord _word) {
        word = _word;
        count = 1;
    }

    protected void addCount() {
        count++;
    }

    protected IndexedWord getWord() {
        return word;
    }

    protected Integer getID() {
        return word.getID();
    }

    protected int getCount() {
        return count;
    }

    @Override
    public int compareTo(WordFrequency w) {
        return w.count - count;
    }

}
