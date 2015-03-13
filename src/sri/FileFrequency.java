package sri;

/**
 *
 * @author Fasgort
 */
public class FileFrequency implements Comparable<FileFrequency> {

    final private Integer idFile;
    private int count;

    public FileFrequency(Integer _idFile) {
        idFile = _idFile;
        count = 1;
    }

    public void addCount() {
        count++;
    }

    public Integer getID() {
        return idFile;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(FileFrequency w) {
        return w.count - count;
    }

}
