package sri;

import static java.lang.StrictMath.log;

/**
 *
 * @author Fasgort
 */
public class IndexedWord implements Comparable<IndexedWord> {

    static private Integer idNext = 0;
    final private String word;
    final private Integer idWord;
    private double idf;

    public IndexedWord(String _word) {
        word = _word;
        idWord = idNext++;
        idf = 0.0;
    }

    public String getWord() {
        return word;
    }

    public Integer getID() {
        return idWord;
    }

    public void setIDF(double _idf) {
        idf = _idf;
    }

    public double getIDF() {
        return idf;
    }

    @Override
    public int compareTo(IndexedWord w) {
        return idWord - w.idWord;
    }

}
