package sri;

/**
 *
 * @author Fasgort
 */
public class FileFrequency implements Comparable<FileFrequency> {

    final private IndexedFile file;
    private int count;

    public FileFrequency(IndexedFile _file) {
        file = _file;
        count = 1;
    }

    public void addCount() {
        count++;
    }

    public IndexedFile getFile() {
        return file;
    }

    public Integer getID() {
        return file.getID();
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(FileFrequency w) {
        return w.count - count;
    }

}
