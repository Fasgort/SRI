package sri;

/**
 *
 * @author Fasgort
 */
public class WordFrequency implements Comparable<WordFrequency> {

    final private Integer idWord;
    private int count;

    public WordFrequency(Integer _idWord) {
        idWord = _idWord;
        count = 1;
    }

    public void addCount() {
        count++;
    }

    public Integer getID() {
        return idWord;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(WordFrequency w) {
        return w.count - count;
    }

}
