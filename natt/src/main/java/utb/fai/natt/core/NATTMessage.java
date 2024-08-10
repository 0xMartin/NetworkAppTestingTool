package utb.fai.natt.core;

import utb.fai.natt.spi.INATTMessage;

/**
 * Jedna se o zpravu, ktera je prijata v libovolnem modulu od testovane
 * aplikace libovolnym zpusobem.
 */
public class NATTMessage implements INATTMessage {
    
    // Tag zprava nebo take jeji libovolne oznaceni
    private String tag;

    // Textovy obsah zpravy
    private String message;

    public NATTMessage(String tag, String message) {
        this.tag = tag;
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Pokusi se vyhledat text ve zprave podle zvoleneho vyhledavaciho pravidla.
     * 
     * @param searchText    Vyhledavani text
     * @param searchType    Zpusob hledani
     * @param caseSensitive True v pripade pro case sensitive hledani
     * @return True v pripade nalezeni textu se zvolenym zpusobem vyhledavani
     */
    public boolean searchInMessage(String searchText, INATTMessage.SearchType searchType, boolean caseSensitive) {

        String target = this.message;
        if (!caseSensitive) {
            searchText = searchText.toLowerCase();
            target = target.toLowerCase();
        }

        switch (searchType) {
            case EQUALS:
                return target.equals(searchText);
            case CONTAINS:
                return target.contains(searchText);
            case STARTSWITH:
                return target.startsWith(searchText);
            case ENDSWITH:
                return target.endsWith(searchText);
            default:
                return false;
        }
    }
}
