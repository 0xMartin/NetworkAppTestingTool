package utb.fai.natt.spi;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Interface for the message buffer. Provides methods for adding and retrieving messages from NATT modules.
 */
public interface IMessageBuffer {

    /**
     * A constant string used as a postfix for storing the last message of each module.
     * The variable will be named <module-name>-last-msg.
     */
    static String VAR_LAST_MSG_POSTFIX = "-last-msg";

    /**
     * Creates a message buffer for the specified module.
     * The buffer will only be created if the module name is unique and non-empty.
     *
     * @param name The name of the module for which the message buffer is created.
     * @return True if the buffer is successfully created, false otherwise.
     */
    boolean createMessageBufferForModule(String name);

    /**
     * Adds a message to the buffer of the specified module.
     * The content of the message will also be stored in a variable named <module-name>-last-msg.
     *
     * @param name    The name of the module adding the message to the buffer.
     * @param tag     The tag of the message.
     * @param message The content of the message.
     * @return True if the message is successfully added, false otherwise.
     */
    boolean addMessage(String name, String tag, String message);

    /**
     * Clears all message buffers for all modules.
     */
    void clearAll();

    /**
     * Clears the message buffer for a specific module.
     *
     * @param name The name of the module whose buffer will be cleared.
     * @return True if the buffer is successfully cleared, false otherwise.
     */
    boolean clearOneMessageBuffer(String name);

    /**
     * Removes a specific message from the buffer of a given module.
     *
     * @param name    The name of the module.
     * @param message The message to be removed from the buffer.
     */
    void removeMessageFromModule(String name, INATTMessage message);

    /**
     * Retrieves all messages from the buffer of a specific module.
     * This method never returns null. If no messages are found, an empty list is returned.
     *
     * @param name The name of the module.
     * @return A list of messages from the specified module.
     */
    CopyOnWriteArrayList<INATTMessage> getMessages(String name);

    /**
     * Checks whether the buffer contains any messages for the specified module.
     *
     * @param name The name of the module.
     * @return True if the buffer contains messages, false otherwise.
     */
    boolean containsMessage(String name);

    /**
     * Searches for messages in the buffer of a specific module that match the given criteria.
     * If a parameter is empty, it is ignored and all possibilities are accepted.
     *
     * @param name          The name of the module that added the message to the buffer.
     * @param tag           The tag of the message.
     * @param searchText    The text to search for within the message content.
     * @param searchType    The method of searching within the message content (e.g., equals, contains).
     * @param caseSensitive True for case-sensitive searching, false otherwise.
     * @return A list of messages that match the specified criteria.
     */
    LinkedList<INATTMessage> searchMessages(String name, String tag, String searchText, INATTMessage.SearchType searchType, boolean caseSensitive);

}