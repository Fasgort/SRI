package sri;

import java.io.Serializable;

/**
 *
 * @author Fasgort
 */
public class IndexedFile implements Comparable<IndexedFile>, Serializable {

    private static final long serialVersionUID = -4509962320335592519L;
    private static Integer idNext = 0;
    final private String file;
    final private Integer idFile;
    private long checksum = -1;
    private transient int maxFrequentWord;

    protected IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
        maxFrequentWord = 0;
    }

    protected static void setNextID(Integer _idNext) {
        idNext = _idNext;
    }

    protected String getFile() {
        return file;
    }

    protected Integer getID() {
        return idFile;
    }

    protected void setChecksum(long _checksum) {
        checksum = _checksum;
    }

    protected long getChecksum() {
        return checksum;
    }

    protected void setMaxFrequentWord(int count) {
        maxFrequentWord = count;
    }

    protected int getMaxFrequentWord() {
        return maxFrequentWord;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
