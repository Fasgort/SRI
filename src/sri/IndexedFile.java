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
    private double norm = 0.0;

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

    protected void setNorm(double _norm) {
        norm = _norm;
    }

    protected double getNorm() {
        return norm;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
