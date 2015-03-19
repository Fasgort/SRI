package sri;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;
import java.util.Iterator;
import java.util.LinkedList;
import javafx.util.Pair;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    private static DataManager instance = null;
    final private FileDictionary fileDictionary;
    final private WordDictionary wordDictionary;
    SparseIntMatrix2D frequencyIndex;
    SparseFloatMatrix2D weightIndex;

    protected DataManager() {
        ConfigReader configReader = ConfigReader.getInstance();

        fileDictionary = FileDictionary.getInstance();
        wordDictionary = WordDictionary.getInstance();

        SparseIntMatrix2D _frequencyIndex = null;
        SparseFloatMatrix2D _weightIndex = null;

        File serFrequency = new File(configReader.getStringFrequencyIndex());
        if (serFrequency.canRead()) {
            try {
                FileInputStream fis = new FileInputStream(serFrequency);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    _frequencyIndex = (SparseIntMatrix2D) ois.readObject();
                }
            } catch (Exception e) {
                System.out.println("Failed loading serialized frequency index.");
            }
        }

        File serWeight = new File(configReader.getStringWeightIndex());
        if (serWeight.canRead()) {
            try {
                FileInputStream fis = new FileInputStream(serWeight);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    _weightIndex = (SparseFloatMatrix2D) ois.readObject();
                }
            } catch (Exception e) {
                System.out.println("Failed loading serialized weight index.");
            }
        }

        if (_frequencyIndex == null) {
            frequencyIndex = new SparseIntMatrix2D(7500, 300);
        } else {
            frequencyIndex = _frequencyIndex;
        }

        if (_weightIndex == null) {
            weightIndex = new SparseFloatMatrix2D(7500, 300);
        } else {
            weightIndex = _weightIndex;
        }

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public int searchWord(String word) {
        int idWord = wordDictionary.search(word);
        if (idWord == -1) {
            IndexedWord iW = new IndexedWord(word);
            idWord = wordDictionary.add(iW);
        }
        return idWord;
    }

    public IndexedWord searchWord(int idWord) {
        return wordDictionary.search(idWord);
    }

    public int searchFile(String file) {
        int idFile = fileDictionary.search(file);
        if (idFile == -1) {
            IndexedFile iF = new IndexedFile(file);
            idFile = fileDictionary.add(iF);
        }
        return idFile;
    }

    public IndexedFile searchFile(int idFile) {
        return fileDictionary.search(idFile);
    }

    public boolean checksumFile(int idFile, long checksum) {
        IndexedFile iF = fileDictionary.search(idFile);
        iF.doesExist();
        return iF.getChecksum() == checksum;
    }

    public void updateChecksumFile(int idFile, long checksum) {
        IndexedFile iF = fileDictionary.search(idFile);
        iF.setChecksum(checksum);
        iF.setModified(true);
        frequencyIndex.viewColumn(idFile).assign(0);
        weightIndex.viewColumn(idFile).assign(0F);
    }

    public void ignoreFile(int idFile) {
        IndexedFile iF = fileDictionary.search(idFile);
        iF.doesNotExist();
    }

    public void addFrequency(int idWord, int idFile) {
        int count = frequencyIndex.getQuick(idWord, idFile);
        if (count == 0) {
            IndexedWord iW = wordDictionary.search(idWord);
            iW.addDocumentCount();
        }
        frequencyIndex.setQuick(idWord, idFile, count + 1);
    }

    public int getFrequency(int idWord, int idFile) {
        return frequencyIndex.getQuick(idWord, idFile);
    }

    public void generateIndex() {
        ConfigReader configReader = ConfigReader.getInstance();
        int numberDocuments = fileDictionary.size();
        Iterator<IndexedWord> itw;
        Iterator<IndexedFile> itf;

        // Clean removed files
        itf = fileDictionary.iterator();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            if (iF.exists() == false && iF.getChecksum() != -1) {
                // Clean index values related to the file
                itw = wordDictionary.iterator();
                while (itw.hasNext()) {
                    IndexedWord iW = itw.next();
                    int idWord = iW.getID();
                    if (frequencyIndex.getQuick(idWord, iF.getID()) != 0) {
                        iW.subDocumentCount();
                    }
                }
                frequencyIndex.viewColumn(iF.getID()).assign(0);
                weightIndex.viewColumn(iF.getID()).assign(0F);
                iF.setChecksum(-1);
                iF.setModified(true); // Needed to refresh word data

                // Clean old files
                File deletedFile;
                deletedFile = new File(configReader.getStringDirColEnN() + iF.getFile().replace(".html", ".txt"));
                deletedFile.deleteOnExit();
                deletedFile = new File(configReader.getStringDirColEnStop() + iF.getFile().replace(".html", ".txt"));
                deletedFile.deleteOnExit();
                deletedFile = new File(configReader.getStringDirColEnStem() + iF.getFile().replace(".html", ".txt"));
                deletedFile.deleteOnExit();
                deletedFile = new File(configReader.getStringDirColEnSer() + iF.getFile().replace(".html", ".ser"));
                deletedFile.deleteOnExit();
            }
        }

        boolean indexModified = false;

        // Generate IDF & Weight
        itf = fileDictionary.iterator();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            if (!iF.isModified()) {
                continue;
            }
            indexModified = true;
            itw = wordDictionary.iterator();
            int maxFrequency = frequencyIndex.viewColumn(iF.getID()).getMaxLocation()[0];
            float normFile = 0F;
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                int documentsWithWord = iW.getDocumentCount();
                if (documentsWithWord == 0) {
                    iW.setIDF(0F);
                } else {
                    iW.setIDF((float) log((float) numberDocuments / (float) documentsWithWord));
                }
                float fileFrequency = frequencyIndex.getQuick(iW.getID(), iF.getID()) / (float) maxFrequency;
                float weight = fileFrequency * iW.getIDF();
                normFile += pow(weight, 2);
                weightIndex.setQuick(iW.getID(), iF.getID(), weight);
            }
            normFile = (float) sqrt(normFile);
            iF.setNorm(normFile);
            itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                float weight = weightIndex.getQuick(iW.getID(), iF.getID());
                if (weight != 0F) {
                    float normWeight = weight / normFile;
                    weightIndex.setQuick(iW.getID(), iF.getID(), normWeight);
                }
            }
        }

        if ("true".equals(configReader.getSerialize()) && indexModified) {

            try {
                FileOutputStream fos = new FileOutputStream(configReader.getStringFrequencyIndex());
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    frequencyIndex.trimToSize();
                    oos.writeObject(frequencyIndex);
                }
            } catch (Exception e) {
                System.out.println("Failed serializing weight table.");
            }

            try {
                FileOutputStream fos = new FileOutputStream(configReader.getStringWeightIndex());
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    weightIndex.trimToSize();
                    oos.writeObject(weightIndex);
                }
            } catch (Exception e) {
                System.out.println("Failed serializing weight table.");
            }

        }

    }

    public void saveDictionary(String stringDirDictionary) {
        fileDictionary.saveDictionary(stringDirDictionary);
        wordDictionary.saveDictionary(stringDirDictionary);
    }

    public void topFrequentWords(int sizeList) {
        LinkedList<Pair<IndexedWord, Integer>> list = new LinkedList();
        Iterator<IndexedWord> itw = wordDictionary.accessDictionary().iterator();

        int minFrequency = 0;
        while (itw.hasNext()) {
            IndexedWord wordA = itw.next();
            int wordAFrequency = frequencyIndex.viewRow(wordA.getID()).zSum();
            if (wordAFrequency > minFrequency || list.size() < sizeList) {
                if (list.size() == 0) {
                    list.add(new Pair(wordA, wordAFrequency));
                } else {
                    Iterator<Pair<IndexedWord, Integer>> itList = list.descendingIterator();
                    int i = list.size();
                    boolean added = false;
                    while (itList.hasNext()) {
                        int wordBFrequency = itList.next().getValue();
                        if (wordAFrequency <= wordBFrequency) {
                            list.add(i, new Pair(wordA, wordAFrequency));
                            added = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                    if (!added) {
                        list.addFirst(new Pair(wordA, wordAFrequency));
                    }
                }
                if (list.size() > sizeList) {
                    minFrequency = list.getLast().getValue();
                    list.removeLast();
                }
            }
        }

        for (int i = 0; i < sizeList; i++) {
            Pair<IndexedWord, Integer> word = list.removeFirst();
            System.out.println("   " + word.getKey().getWord() + " with " + word.getValue() + " apparitions in documents.");
        }

    }

    public int wordQuantity() {
        return wordDictionary.size();
    }

    public int fileQuantity() {
        return fileDictionary.size();
    }

}
