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
    private transient int maxCount = 0;
    private transient boolean builtFrequency = false;

    protected IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
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

    protected void updateMaxCount(int count) {
        if (count > maxCount) {
            maxCount = count;
        }
    }

    protected int getMaxCount() {
        return maxCount;
    }

    protected void setBuiltFrequency() {
        builtFrequency = true;
    }

    protected boolean getBuiltFrequency() {
        return builtFrequency;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
