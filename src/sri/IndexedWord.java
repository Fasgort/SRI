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
    private int idWord;
    private float idf;
    private int documentCount;
    private int frequency;
    private transient boolean exists = false;

    protected IndexedWord(String _word) {
        word = _word;
        idWord = idNext++;
        idf = 0F;
        documentCount = 0;
        frequency = 0;
    }

    protected static void setNextID(int _idNext) {
        idNext = _idNext;
    }

    protected String getWord() {
        return word;
    }

    protected void setID(int _idWord) {
        idWord = _idWord;
    }

    protected int getID() {
        return idWord;
    }

    protected void setIDF(float _idf) {
        idf = _idf;
    }

    protected float getIDF() {
        return idf;
    }

    protected void setDocumentCount(int count) {
        documentCount = count;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public int getFrequency() {
        return frequency;
    }

    public void sumFrequency(int sum) {
        frequency += sum;
    }

    public void lessFrequency(int less) {
        frequency -= less;
    }

    protected void doesExist() {
        exists = true;
    }

    protected void doesNotExist() {
        exists = false;
    }

    protected boolean exists() {
        return exists;
    }

    @Override
    public int compareTo(IndexedWord w) {
        return idWord - w.idWord;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedWord other = (IndexedWord) obj;
        return idWord == other.idWord;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.idWord;
        return hash;
    }

}
