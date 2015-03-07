package sri;

/**
 *
 * @author Fasgort
 */
public class Word implements Comparable<Word> {

    static private Integer idNext = 0;
    final private String word;
    final private Integer idWord;

    public Word(String _word) {
        word = _word;
        idWord = idNext++;
    }

    public String getWord() {
        return word;
    }

    public Integer getID() {
        return idWord;
    }

    @Override
    public int compareTo(Word w) {
        return idWord - w.idWord;
    }

}
