package sri;

/**
 *
 * @author Fasgort
 */
public class FrequentWord implements Comparable<FrequentWord> {

    final private Integer idWord;
    private int count;

    public FrequentWord(Integer _idWord) {
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
    public int compareTo(FrequentWord w) {
        return w.count - count;
    }

}
