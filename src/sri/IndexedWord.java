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
    private int documentCount;
    private double idf;

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
