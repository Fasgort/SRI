package sri;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
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
    private SparseDoubleMatrix2D index;

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
        IndexedFile iF = fileDictionary.search(idFile);
        FileFrequency ff = wd.search(iF);
        if (ff == null) {
            wd.add(iF);
        } else {
            ff.addCount();
            wd.addCount();
        }

        FileData fd = fileData.get(idFile);
        IndexedWord iW = wordDictionary.search(idWord);
        WordFrequency wf = fd.search(iW);
        if (wf == null) {
            fd.add(iW);
        } else {
            wf.addCount();
            fd.addCount();
        }
    }

    public void generateIndex() {
        int numberWords = wordDictionary.size();
        int numberDocuments = fileDictionary.size();
        Iterator<IndexedWord> itw;
        Iterator<IndexedFile> itf;

        // Initialize index
        index = new SparseDoubleMatrix2D(numberWords, numberDocuments);

        // Generate IDF
        itw = wordDictionary.iterator();
        while (itw.hasNext()) {
            IndexedWord iW = itw.next();
            int documentsWithWord = wordData.get(iW.getID()).size();
            iW.setIDF(log((double) numberDocuments / (double) documentsWithWord));
        }

        // Generate Weight
        itw = wordDictionary.iterator();
        while (itw.hasNext()) {
            IndexedWord iW = itw.next();
            itf = fileDictionary.iterator();
            double normFile = 0;
            while (itf.hasNext()) {
                IndexedFile iF = itf.next();
                FileFrequency ff = wordData.get(iW.getID()).search(iF);
                double weight;
                if (ff == null) {
                    weight = 0.0;
                } else {
                    weight = (double) ff.getCount() * (double) iW.getIDF();
                }
                normFile += pow(weight, 2);
                index.set(iW.getID(), iF.getID(), weight);
            }
            normFile = sqrt(normFile);
            itf = fileDictionary.iterator();
            while (itf.hasNext()) {
                IndexedFile iF = itf.next();
                double normWeight = index.get(iW.getID(), iF.getID()) / normFile;
                index.set(iW.getID(), iF.getID(), normWeight);
            }
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
            if (word1.getCount() > minFrequency || list.size() < sizeList) {
                if (list.size() == 0) {
                    list.add(word1);
                } else {
                    Iterator<WordData> listIterator = list.descendingIterator();
                    int i = list.size();
                    boolean added = false;
                    while (listIterator.hasNext()) {
                        WordData word2 = listIterator.next();
                        if (word1.getCount() <= word2.getCount()) {
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
                    minFrequency = list.getLast().getCount();
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
