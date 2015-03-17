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

    public IndexedWord(String _word) {
        word = _word;
        idWord = idNext++;
        documentCount = 0;
        idf = 0;
    }

    public String getWord() {
        return word;
    }

    public Integer getID() {
        return idWord;
    }

    public void addDocument() {
        documentCount++;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void generateIDF(int numberDocuments) {
        idf = log((double) numberDocuments / (double) documentCount);
    }

    public double getIDF() {
        return idf;
    }

    @Override
    public int compareTo(IndexedWord w) {
        return idWord - w.idWord;
    }

}
