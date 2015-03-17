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
    private long checksum;
    private transient int maxCount;
    private transient boolean builtFrequency;

    public IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
        checksum = -1;
        maxCount = 1;
        builtFrequency = false;
    }

    public String getFile() {
        return file;
    }

    public Integer getID() {
        return idFile;
    }

    public void setChecksum(long _checksum) {
        checksum = _checksum;
    }

    public long getChecksum() {
        return checksum;
    }

    public void updateMaxCount(int count) {
        if (count > maxCount) {
            maxCount = count;
        }
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setBuiltFrequency() {
        builtFrequency = true;
    }

    public boolean getBuiltFrequency() {
        return builtFrequency;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
