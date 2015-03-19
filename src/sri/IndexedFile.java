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
    final private int idFile;
    private long checksum = -1;
    private double norm = 0.0;
    transient private boolean exists = false;
    transient private boolean modified = false;

    protected IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
        exists = true;
        modified = true;
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

    protected void setChecksum(long _checksum) {
        checksum = _checksum;
    }

    protected long getChecksum() {
        return checksum;
    }

    protected void setNorm(double _norm) {
        norm = _norm;
    }

    protected double getNorm() {
        return norm;
    }

    public void doesExist() {
        exists = true;
    }

    public void doesNotExist() {
        exists = false;
    }

    public boolean exists() {
        return exists;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean _modified) {
        modified = _modified;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
