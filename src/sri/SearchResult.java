package sri;

/**
 *
 * @author Fasgort
 */
public class SearchResult implements Comparable<SearchResult> {

    final private IndexedFile document;
    final private float similitude;

    public SearchResult(IndexedFile file, float _similitude) {
        document = file;
        similitude = _similitude;
    }

    public IndexedFile getDocument() {
        return document;
    }

    public float getSimilitude() {
        return similitude;
    }

    @Override
    public int compareTo(SearchResult b) {
        if (similitude >= b.similitude) {
            if (similitude == b.similitude) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return 1;
        }
    }

}
