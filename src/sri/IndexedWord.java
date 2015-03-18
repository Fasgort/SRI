package sri;

import java.io.Serializable;

/**
 *
 * @author Fasgort
 */
public class IndexedWord implements Comparable<IndexedWord>, Serializable {

    private static final long serialVersionUID = 8769534561148736806L;
    private static int idNext = 0;
    final private String word;
    final private int idWord;
    private double idf;

    protected IndexedWord(String _word) {
        word = _word;
        idWord = idNext++;
        idf = 0.0;
    }

    protected static void setNextID(int _idNext) {
        idNext = _idNext;
    }

    protected String getWord() {
        return word;
    }

    protected int getID() {
        return idWord;
    }

    protected void setIDF(double _idf) {
        idf = _idf;
    }

    protected double getIDF() {
        return idf;
    }

    @Override
    public int compareTo(IndexedWord w) {
        return idWord - w.idWord;
    }

}
