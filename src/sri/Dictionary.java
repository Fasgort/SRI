package sri;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import javafx.util.Pair;

/**
 *
 * @author Fasgort
 * @param <T1>
 * @param <T2>
 */
public abstract class Dictionary<T1, T2> {

    protected ArrayList<T1> entryIDs; // Entry Dictionary ID -> entry
    protected ConcurrentMap<T2, Integer> entries; // Entry Dictionary entry -> ID
    protected transient boolean dirty = false;

    protected int search(T2 entry) {
        Integer idEntry = entries.get(entry);
        if (idEntry == null) {
            return -1;
        }
        return idEntry;
    }

    protected T1 search(int idEntry) {
        if (idEntry < entryIDs.size()) {
            return entryIDs.get(idEntry);
        } else {
            return null;
        }
    }

    protected abstract int add(T1 newEntry);

    protected abstract void replaceAndDelete(int oldID, int newID);

    protected abstract void saveDictionary();

    protected abstract void doesExist(int ID);

    protected abstract void doesNotExist(int ID);

    protected abstract boolean exists(int ID);

    protected void setDirty() {
        dirty = true;
    }

    protected boolean isDirty() {
        return dirty;
    }

    protected int size() {
        return entryIDs.size();
    }

    protected Pair<ArrayList<T1>, ConcurrentMap<T2, Integer>> accessDictionary() {
        return new Pair(entryIDs, entries);
    }

    protected Iterator<T1> iterator() {
        return entryIDs.iterator();
    }

}
