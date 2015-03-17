package sri;

/**
 *
 * @author Fasgort
 */
public class FileFrequency implements Comparable<FileFrequency> {

    final private IndexedFile file;
    private int count;

    protected FileFrequency(IndexedFile _file) {
        file = _file;
        count = 1;
    }

    public void addCount() {
        count++;
    }

    protected IndexedFile getFile() {
        return file;
    }

    protected Integer getID() {
        return file.getID();
    }

    protected int getCount() {
        return count;
    }

    @Override
    public int compareTo(FileFrequency w) {
        return w.count - count;
    }

}
