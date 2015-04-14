package sri;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Fasgort
 * @param <T>
 */
public abstract class Dictionary<T> {

    protected ArrayList<T> entryIDs; // Entry Dictionary ID -> entry
    protected Map<String, Integer> entries; // Entry Dictionary entry -> ID
    protected transient BitSet exists;
    protected transient int bitsetSize;
    protected transient boolean dirty = false;

    protected int search(String entry) {
        Integer idEntry = entries.get(entry);
        if (idEntry == null) {
            return -1;
        }
        return idEntry;
    }

    protected T search(int idEntry) {
        if (idEntry < entryIDs.size()) {
            return entryIDs.get(idEntry);
        } else {
            return null;
        }
    }

    protected abstract int add(T newEntry);

    protected abstract void move(int oldID, int newID);

    protected abstract void cleanDictionary();

    protected abstract void saveDictionary();

    protected void doesExist(int idEntry) {
        if (idEntry <= bitsetSize) {
            exists.set(idEntry);
        }
    }

    protected void doesNotExist(int idEntry) {
        if (idEntry <= bitsetSize) {
            exists.clear(idEntry);
        }
    }

    protected boolean exists(int idEntry) {
        if (idEntry <= bitsetSize) {
            return exists.get(idEntry);
        } else {
            return true;
        }
    }

    protected void setDirty() {
        dirty = true;
    }

    protected BitSet getExistBitset() {
        return exists;
    }

    protected void setExistBitset(BitSet bitset) {
        exists = bitset;
    }

    protected int getBitsetSize() {
        return bitsetSize;
    }

    protected void setBitsetSize(int size) {
        bitsetSize = size;
    }

    protected int size() {
        return entryIDs.size();
    }

    protected int cardinality() {
        return exists.cardinality();
    }

    protected Iterator<T> iterator() {
        return entryIDs.iterator();
    }

}
