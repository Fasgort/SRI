package sri;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    private static DataManager instance = null;
    final private FileDictionary fileDictionary;
    final private WordDictionary wordDictionary;
    ArrayList<WordData> wordData;
    ArrayList<FileData> fileData;

    protected DataManager() {
        fileDictionary = FileDictionary.getInstance();
        wordDictionary = WordDictionary.getInstance();
        wordData = new ArrayList(5000);
        fileData = new ArrayList(300);

        ArrayList<IndexedWord> wordDic = wordDictionary.accessDictionary();
        for (int i = 0; i < wordDic.size(); i++) {
            IndexedWord iW = wordDic.get(i);
            wordData.add(i, new WordData(iW));
        }

        ArrayList<IndexedFile> fileDic = fileDictionary.accessDictionary();
        for (int i = 0; i < fileDic.size(); i++) {
            IndexedFile iF = fileDic.get(i);
            fileData.add(i, new FileData(iF));
        }

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public Integer searchWord(String word) {
        Integer idWord = wordDictionary.search(word);
        if (idWord == null) {
            IndexedWord iW = new IndexedWord(word);
            idWord = wordDictionary.add(iW);
            wordData.add(idWord, new WordData(iW));
        }
        return idWord;
    }

    public IndexedWord searchWord(Integer idWord) {
        return wordDictionary.search(idWord);
    }

    public Integer searchFile(String file) {
        Integer idFile = fileDictionary.search(file);
        if (idFile == null) {
            IndexedFile iF = new IndexedFile(file);
            idFile = fileDictionary.add(iF);
            fileData.add(idFile, new FileData(iF));
        }
        return idFile;
    }

    public IndexedFile searchFile(Integer idFile) {
        return fileDictionary.search(idFile);
    }

    public boolean checksumFile(Integer idFile, long checksum) {
        return fileDictionary.search(idFile).getChecksum() == checksum;
    }

    public void updateChecksumFile(Integer idFile, long checksum) {
        fileDictionary.search(idFile).setChecksum(checksum);
    }

    public void addFrequency(Integer idWord, Integer idFile) {
        WordData wd = wordData.get(idWord);
        wd.add(idFile);

        FileData fd = fileData.get(idFile);
        fd.add(idFile);
    }

    public void generateIndex() {
        int numberWords = wordDictionary.size();
        int numberDocuments = fileDictionary.size();
        Iterator<IndexedWord> itw;
        Iterator<IndexedFile> itf;

        // Initialize index
        SparseDoubleMatrix2D index = new SparseDoubleMatrix2D(numberWords, numberDocuments);

        // Generate IDF
        itw = wordDictionary.iterator();
        while (itw.hasNext()) {
            IndexedWord iW = itw.next();
            int documentsWithWord = wordData.get(iW.getID()).cardinality();
            iW.setIDF(log((double) numberDocuments / (double) documentsWithWord));
        }

        // Generate Weight
        itf = fileDictionary.iterator();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            itw = wordDictionary.iterator();
            double normFile = 0;
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                int fileFrequency = wordData.get(iW.getID()).search(iF.getID());
                double weight = (double) fileFrequency * (double) iW.getIDF();
                normFile += pow(weight, 2);
                index.setQuick(iW.getID(), iF.getID(), weight);
            }
            normFile = sqrt(normFile);
            iF.setNorm(normFile);
            itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                double normWeight = index.getQuick(iW.getID(), iF.getID()) / normFile;
                index.setQuick(iW.getID(), iF.getID(), normWeight);
            }
        }

        SparseRCDoubleMatrix2D compressedIndex = index.getRowCompressed(false);

        try {
            ConfigReader configReader = ConfigReader.getInstance();

            FileOutputStream fos = new FileOutputStream(configReader.getStringWeightIndex());
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(compressedIndex);
            }
        } catch (Exception e) {
            System.out.println("Failed serializing weight table.");
        }

    }

    public void saveDictionary(String stringDirDictionary) {
        fileDictionary.saveDictionary(stringDirDictionary);
        wordDictionary.saveDictionary(stringDirDictionary);
    }

    public LinkedList<WordData> topFrequentWords(int sizeList) {
        LinkedList<WordData> list = new LinkedList();
        Iterator<WordData> wordIterator = wordData.iterator();

        int minFrequency = 0;
        while (wordIterator.hasNext()) {
            WordData word1 = wordIterator.next();
            if (word1.getWordCount() > minFrequency || list.size() < sizeList) {
                if (list.size() == 0) {
                    list.add(word1);
                } else {
                    Iterator<WordData> listIterator = list.descendingIterator();
                    int i = list.size();
                    boolean added = false;
                    while (listIterator.hasNext()) {
                        WordData word2 = listIterator.next();
                        if (word1.getWordCount() <= word2.getWordCount()) {
                            list.add(i, word1);
                            added = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                    if (!added) {
                        list.addFirst(word1);
                    }
                }
                if (list.size() > sizeList) {
                    minFrequency = list.getLast().getWordCount();
                    list.removeLast();
                }
            }
        }

        return list;

    }

    public Integer wordQuantity() {
        return wordData.size();
    }

    public Integer fileQuantity() {
        return fileData.size();
    }

}
