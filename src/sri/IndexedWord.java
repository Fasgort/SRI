package sri;

import java.io.Serializable;
import static java.lang.StrictMath.log;

/**
 *
 * @author Fasgort
 */
public class IndexedWord implements Comparable<IndexedWord>, Serializable {

    private static final long serialVersionUID = 8769534561148736806L;
    private static Integer idNext = 0;
    final private String word;
    final private Integer idWord;
    private transient int documentCount;
    private transient double idf;

    protected IndexedWord(String _word) {
        word = _word;
        idWord = idNext++;
        documentCount = 0;
        idf = 0;
    }

    protected static void setNextID(Integer _idNext) {
        idNext = _idNext;
    }

    protected String getWord() {
        return word;
    }

    protected Integer getID() {
        return idWord;
    }

    protected void addDocument() {
        documentCount++;
    }

    protected int getDocumentCount() {
        return documentCount;
    }

    protected void generateIDF(int numberDocuments) {
        idf = log((double) numberDocuments / (double) documentCount);
    }

    protected double getIDF() {
        return idf;
    }

    @Override
    public int compareTo(IndexedWord w) {
        return idWord - w.idWord;
    }

}
