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

    public void generateFrequency() {
        if (!file.getBuiltFrequency()) {
            frequency = (double) count / (double) file.getMaxCount();
        }
    }

    public double getFrequency() {
        return frequency;
    }

    public void generateWeight(double idf) {
        weight = frequency * idf;
    }

    public double getWeight() {
        return weight;
    }

    public void generateNormalizedWeight(double weightSum) {
        weight = weight / weightSum;
    }

    @Override
    public int compareTo(FileFrequency w) {
        return w.count - count;
    }

}
