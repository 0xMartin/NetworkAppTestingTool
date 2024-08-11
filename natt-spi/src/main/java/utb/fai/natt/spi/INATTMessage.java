package utb.fai.natt.spi;

/**
 * Interface for the NATT message. Messages are used in IMessageBuffer.
 */
public interface INATTMessage {

    /**
     * Enumeration for the type of search.
     */
    public static enum SearchType {
        EQUALS,
        CONTAINS,
        STARTSWITH,
        ENDSWITH,
        NONE
    }

    /**
     * Retrieves the tag or label of the message.
     * 
     * @return The tag of the message.
     */
    String getTag();

    /**
     * Retrieves the content of the message in text form.
     * 
     * @return The content of the message.
     */
    String getMessage();

    /**
     * Attempts to search for text within the message according to the specified search rule.
     * 
     * @param searchText    The text to search for.
     * @param searchType    The type of search to perform.
     * @param caseSensitive True if the search should be case-sensitive.
     * @return True if the text is found using the specified search method.
     */
    boolean searchInMessage(String searchText, SearchType searchType, boolean caseSensitive);
}