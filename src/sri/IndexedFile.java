package sri;

/**
 *
 * @author Fasgort
 */
public class IndexedFile implements Comparable<IndexedFile> {

    static private Integer idNext = 0;
    final private String file;
    final private Integer idFile;

    public IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
    }

    public String getFile() {
        return file;
    }

    public Integer getID() {
        return idFile;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}