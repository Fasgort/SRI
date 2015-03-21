package sri;

import java.io.Serializable;

/**
 *
 * @author Fasgort
 */
public class IndexedFile implements Comparable<IndexedFile>, Serializable {

    private static final long serialVersionUID = -4509962320335592519L;
    private static int idNext = 0;
    final private String file;
    private int idFile;
    private long checksum = -1;
    private float norm = 0F;

    protected IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
    }

    protected static void setNextID(int _idNext) {
        idNext = _idNext;
    }

    protected String getFile() {
        return file;
    }

    protected int getID() {
        return idFile;
    }

    protected void setID(int _idFile) {
        idFile = _idFile;
    }

    protected void setChecksum(long _checksum) {
        checksum = _checksum;
    }

    protected long getChecksum() {
        return checksum;
    }

    protected void setNorm(float _norm) {
        norm = _norm;
    }

    protected float getNorm() {
        return norm;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
