package utb.fai.natt.spi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface for the NATT context. Provides access to various components and
 * functionalities
 * used throughout the testing tool.
 */
public interface INATTContext {

    /**
     * Registers a new keyword to the NATT context.
     * 
     * @param name    The name under which the keyword will be available.
     * @param keyword The keyword class.
     * @return True if registration was successful, false otherwise.
     */
    boolean registerKeyword(String name, Class<? extends NATTKeyword> keyword);

    /**
     * Registers a new module to the NATT context.
     * 
     * @param name   The name under which the module will be registered.
     * @param module The class of the new module.
     * @return True if registration was successful, false otherwise.
     */
    boolean registerModule(String name, Class<? extends NATTModule> module);

    /**
     * Gets a reference to the list of all registered keywords.
     * 
     * @return A map of keyword names to their classes.
     */
    HashMap<String, Class<?>> getKeywordSet();

    /**
     * Creates a new instance of the NATT module. The module must be registered in
     * the context. This method must be used in plugins to create instances of the
     * module.
     * 
     * @param moduleName The type name of the module, under which the module is
     *                   registered.
     * @param types      The types of the arguments to be passed to the NATT module
     * @param args       The arguments to be passed to the NATT module constructor.
     * @return An instance of the module.
     */
    NATTModule createInstanceOfModule(String moduleName, Class<?>[] types, Object[] args);

    /**
     * Gets a reference to the list of active modules (they already have been
     * instantiated by some keyword).
     * 
     * @return List of active modules
     */
    LinkedList<NATTModule> getActiveModules();

    /**
     * Remove a specific module from active modules.
     * 
     * @param name The name of the module to remove
     * @return true if the module was removed, false otherwise
     */
    public boolean removeActiveModule(String name);

    /**
     * Safely retrieves a reference to a specific module by name.
     * 
     * @param name The name of the module to find
     * @return The module reference, or null if not found
     */
    NATTModule getActiveModule(String name);

    /**
     * Gets the message buffer which contains messages from all modules and external
     * standard streams.
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
     * @param name  The name of the variable (must be a single word, spaces replaced
     *              with "_")
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