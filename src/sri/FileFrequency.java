package sri;

/**
 *
 * @author Fasgort
 */
public class FileFrequency implements Comparable<FileFrequency> {

    final private IndexedFile file;
    private int count;
    private double frequency;

    public FileFrequency(IndexedFile _file) {
        file = _file;
        count = 1;
        frequency = 0;
    }

    public void addCount() {
        file.updateMaxCount(++count);
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

    public void generateFrequency(int maxCount) {
        frequency = (double) count / (double) maxCount;
    }

    public double getFrequency() {
        return frequency;
    }

    @Override
    public int compareTo(FileFrequency w) {
        return w.count - count;
    }

}
