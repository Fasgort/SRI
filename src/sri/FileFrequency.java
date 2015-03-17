package sri;

/**
 *
 * @author Fasgort
 */
public class FileFrequency implements Comparable<FileFrequency> {

    final private IndexedFile file;
    private int count;
    private double frequency;
    private double weight;

    protected FileFrequency(IndexedFile _file) {
        file = _file;
        count = 1;
        frequency = 0;
    }

    protected void addCount() {
        file.updateMaxCount(++count);
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

    protected void generateFrequency() {
        if (!file.getBuiltFrequency()) {
            frequency = (double) count / (double) file.getMaxCount();
        }
    }

    protected double getFrequency() {
        return frequency;
    }

    protected void generateWeight(double idf) {
        weight = frequency * idf;
    }

    protected double getWeight() {
        return weight;
    }

    protected void generateNormalizedWeight(double weightSum) {
        weight = weight / weightSum;
    }

    @Override
    public int compareTo(FileFrequency w) {
        return w.count - count;
    }

}
