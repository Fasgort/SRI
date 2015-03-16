package sri;

/**
 *
 * @author Fasgort
 */
public class IndexedFile implements Comparable<IndexedFile> {

    static private Integer idNext = 0;
    final private String file;
    final private Integer idFile;
    private int maxCount;

    public IndexedFile(String _file) {
        file = _file;
        idFile = idNext++;
        maxCount = 1;
    }

    public String getFile() {
        return file;
    }

    public Integer getID() {
        return idFile;
    }
    
    public void updateMaxCount(int count){
        if (count > maxCount) maxCount = count;
    }
    
    public int getMaxCount(){
        return maxCount;
    }

    @Override
    public int compareTo(IndexedFile w) {
        return idFile - w.idFile;
    }

}
