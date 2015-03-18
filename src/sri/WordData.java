package sri;

import cern.colt.matrix.tint.impl.SparseIntMatrix1D;

/**
 *
 * @author Fasgort
 */
public class WordData {

    final private IndexedWord word;
    final private SparseIntMatrix1D fileFrequency;
    private int wordCount = 0;

    protected WordData(IndexedWord _word) {
        word = _word;
        fileFrequency = new SparseIntMatrix1D(7500);
    }

    protected void add(int fileID) {
        try {
            fileFrequency.set(fileID, fileFrequency.getQuick(fileID) + 1);
            wordCount++;
        } catch (Exception e) {
            fileFrequency.setSize(fileID * 2);
        } finally {
            fileFrequency.set(fileID, fileFrequency.getQuick(fileID) + 1);
            wordCount++;
        }
    }

    protected int search(int fileID) {
        return fileFrequency.getQuick(fileID);
    }

    protected String getWord() {
        return word.getWord();
    }

    protected Integer getID() {
        return word.getID();
    }

    protected int getWordCount() {
        return wordCount;
    }

    protected int cardinality() {
        return fileFrequency.cardinality();
    }

}
