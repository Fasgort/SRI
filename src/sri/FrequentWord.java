package sri;

/**
 *
 * @author Fasgort
 */
public class FrequentWord implements Comparable<FrequentWord> {

    final private String word;
    private int count;

    public FrequentWord(String _word) {
        word = _word;
        count = 1;
    }

    public void addCount() {
        count++;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(FrequentWord w) {
        return w.count - count;
    }

}
