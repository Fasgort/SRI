package sri;

/**
 *
 * @author Fasgort
 */
public class IndexedWord implements Comparable<IndexedWord> {

    static private Integer idNext = 0;
    final private String word;
    final private Integer idWord;

    public IndexedWord(String _word) {
        word = _word;
        idWord = idNext++;
    }

    public String getWord() {
        return word;
    }

    public Integer getID() {
        return idWord;
    }

    @Override
    public int compareTo(IndexedWord w) {
        return idWord - w.idWord;
    }

}
