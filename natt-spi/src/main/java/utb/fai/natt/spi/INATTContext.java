package utb.fai.natt.spi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface for the NATT context. Provides access to various components and functionalities
 * used throughout the testing tool.
 */
public interface INATTContext {

    /**
     * Gets a reference to the list of all registered keywords.
     */
    HashMap<String, Class<?>> getKeywordSet();

    /**
     * Registers a new keyword to the context.
     * 
     * @param keyword The keyword to register
     */
    boolean registerKeyword(NATTKeyword keyword);

    /**
     * Gets a reference to the list of active modules.
     * 
     * @return List of active modules
     */
    LinkedList<NATTModule> getModules();

    /**
     * Safely retrieves a reference to a specific module by name.
     * 
     * @param name The name of the module to find
     * @return The module reference, or null if not found
     */
    NATTModule getModule(String name);

    /**
     * Gets the message buffer which contains messages from all modules and external standard streams.
     * 
     * @return IMessageBuffer
     */
    IMessageBuffer getMessageBuffer();

    /**
     * Gets all created variables.
     * 
     * @return Map of variables
     */
    ConcurrentHashMap<String, String> getVariables();

    /**
     * Clears all variables.
     */
    void clearVariables();

    /**
     * Retrieves the value of a specific variable.
     * 
     * @param name The name of the variable
     * @return The value of the variable
     */
    String getVariable(String name);

    /**
     * Stores a value in a variable.
     * 
     * @param name  The name of the variable (must be a single word, spaces replaced with "_")
     * @param value The value to store (null will be replaced with an empty string)
     * @return The name of the variable if successfully created, otherwise null
     */
    String storeValueToVariable(String name, String value);

    /**
     * Saves the current state of variables to history for future restoration.
     */
    void saveVariablesState();

    /**
     * Restores variables to the last saved state from history.
     */
    void restoreVariablesState();

}