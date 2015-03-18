package sri;

import cern.colt.matrix.tint.impl.SparseIntMatrix1D;

/**
 *
 * @author Fasgort
 */
public class FileData {

    final private IndexedFile file;
    final private SparseIntMatrix1D wordFrequency;

    public FileData(IndexedFile _file) {
        file = _file;
        wordFrequency = new SparseIntMatrix1D(5000);
    }

    protected void add(int wordID) {
        try {
            wordFrequency.set(wordID, wordFrequency.getQuick(wordID) + 1);
        } catch (Exception e) {
            wordFrequency.setSize(wordID * 2);
        } finally {
            wordFrequency.set(wordID, wordFrequency.getQuick(wordID) + 1);
        }
    }

    protected int search(int wordID) {
        return wordFrequency.getQuick(wordID);
    }

    protected String getFile() {
        return file.getFile();
    }

    protected Integer getID() {
        return file.getID();
    }

    protected int cardinality() {
        return wordFrequency.cardinality();
    }

}
