package sri;

/**
 *
 * @author Fasgort
 */
public class IndexedFile implements Comparable<IndexedFile> {

    static private Integer idNext = 0;
    final private String file;
    final private Integer idFile;
    private int maxFrequentWord;

    public IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
        maxFrequentWord = 0;
    }

    public String getFile() {
        return file;
    }

    public Integer getID() {
        return idFile;
    }

    public void setMaxFrequentWord(int count) {
            maxFrequentWord = count;
    }

    public int getMaxFrequentWord() {
        return maxFrequentWord;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
