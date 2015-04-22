package sri;

import java.io.Serializable;

/**
 *
 * @author Fasgort
 */
public class IndexedFile implements Comparable<IndexedFile>, Serializable {

    private static final long serialVersionUID = -4509962320335592519L;
    private static int idNext = 0;
    private int idFile;
    private final long checksum;
    private String file;
    private String title;
    private float norm = 0F;
    private transient boolean exists = false;
    private transient boolean isNew = false;
    private transient boolean named = false;

    protected IndexedFile(long _checksum) {
        checksum = _checksum;
        idFile = idNext++;
        exists = true;
        isNew = true;
    }

    protected static void setNextID(int _idNext) {
        idNext = _idNext;
    }

    protected void setID(int _idFile) {
        idFile = _idFile;
    }

    protected int getID() {
        return idFile;
    }

    protected long getChecksum() {
        return checksum;
    }

    protected void setFile(String _file) {
        named = true;
        file = _file;
    }

    protected String getFile() {
        return file;
    }

    protected void setTitle(String _title) {
        title = _title;
    }

    protected String getTitle() {
        return title;
    }

    protected void setNorm(float _norm) {
        norm = _norm;
    }

    protected float getNorm() {
        return norm;
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

    protected boolean isNew() {
        return isNew;
    }

    protected boolean isNamed() {
        return named;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedFile other = (IndexedFile) obj;
        return idFile == other.idFile;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.idFile;
        return hash;
    }

}
