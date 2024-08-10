package utb.fai.natt.spi;

import java.util.ArrayList;
import java.util.List;

import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

/**
 * Abstract class for basic module access
 */
public abstract class NATTModule {

    /**
     * Allows setting action filtering
     */
    public static class MessageFilter {
        /** Desired text that should appear in the message in a specific format */
        public String text;

        /** Desired message tag. Set to null if ignored */
        public String tag;

        /** Text search mode: equals, contains, startswith, endswith */
        public String mode;

        /** True if case-sensitive. Default value is true. */
        public Boolean caseSensitive;

        public MessageFilter(String text, String tag, String mode, Boolean caseSensitive) {
            this.text = text;
            this.tag = tag;
            this.mode = mode;
            this.caseSensitive = caseSensitive;
        }
    }

    // Contains the module type name
    private String moduleTypeName;

    // Contains the name of the object. Used to distinguish multiple identical modules.
    private String name;

    // True if the module is currently running
    private boolean isRunning;

    // List of message listeners
    private List<IMessageListener> messageListeners = new ArrayList<IMessageListener>();

    // List of filters
    private List<MessageFilter> filters = new ArrayList<MessageFilter>();

    public NATTModule(String name, INATTContext nattCtx) throws NonUniqueModuleNamesException, InternalErrorException {
        if (name == null) {
            throw new InternalErrorException("Name of module is null");
        }
        if (name.isEmpty()) {
            throw new InternalErrorException("Name of module is empty");
        }

        // Module name
        this.name = name;

        this.isRunning = false;

        // Module type name
        Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(NATTAnnotation.Module.class)) {
            NATTAnnotation.Module annotation = clazz.getAnnotation(NATTAnnotation.Module.class);
            this.moduleTypeName = annotation.value();
        }

        // Create message buffer for this module
        nattCtx.getMessageBuffer().createMessageBufferForModule(this.name);

        // Add module to the list of active modules in the context
        this.addToListIfNameIsUnique(nattCtx.getModules());
    }

    /**
     * Gets the name of this module. This is the name that distinguishes this
     * module from others.
     * 
     * @return Name of the module
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the module is active
     * 
     * @return True if the module is active
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Sets the state of whether the module is active or not
     * 
     * @param isRunning State
     */
    protected void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * Gets the module type name. This is the name of its type
     * 
     * @return Name of the module type
     */
    public String getModuleTypeName() {
        return moduleTypeName;
    }

    /**
     * Returns the list of action filters
     * 
     * @return List of filters
     */
    public List<MessageFilter> getActionFilterList() {
        return this.filters;
    }

    /**
     * Notifies all listening classes of a message received by this module
     * 
     * @param tag     Tag of the message
     * @param message Content of the message in text form
     */
    protected void notifyMessageListeners(String tag, String message) {
        // Filtering
        if (!this.filters.isEmpty()) {
            for (MessageFilter f : this.filters) {
                if (f.tag != null) {
                    if (!f.tag.equals(tag)) {
                        return;
                    }
                }
                if (!NATTAssert.assertCondition(
                        message,
                        f.text,
                        f.mode,
                        f.caseSensitive == null ? true : f.caseSensitive)) {
                    return;
                }
            }
        }

        // Trigger actions
        for (IMessageListener listener : messageListeners) {
            if (listener != null) {
                listener.onMessageReceived(this.getName(), tag, message);
            }
        }
    }

    /**
     * Adds a message listener
     * 
     * @param listener IMessageListener
     */
    public void addMessageListener(IMessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * Removes a message listener
     * 
     * @param listener IMessageListener
     */
    public void removeMessageListener(IMessageListener listener) {
        messageListeners.remove(listener);
    }

    public String getNameForLogger() {
        return "(" + this.getName() + ") ";
    }

    /**
     * Abstract method for running the module. Currently used for running the "app.std.in" module,
     * which has a reserved special method.
     * 
     * @throws InternalErrorException
     */
    public abstract void runModule() throws InternalErrorException;

    /**
     * Abstract method for terminating the module. Always called for all activated modules
     * when the execution of a test part where this module was defined is complete.
     * For example, if it was defined within a test case, it will also be terminated at its end.
     * 
     * @return True if the module was successfully terminated
     */
    public abstract boolean terminateModule();

    /**
     * Sends a message from the tested application using this module
     * 
     * @return True if the message was successfully sent to the server. It is not affected
     *         by the potential failure of the message on the communicating counterpart.
     *         It only indicates whether sending the message was successful or not.
     * @throws InternalErrorException
     */
    public abstract boolean sendMessage(String message) throws InternalErrorException;

    /**
     * Adds this module to the provided list of modules if its name is unique within the list
     * 
     * @param modules Reference to the list of modules
     * 
     * @throws NonUniqueModuleNamesException
     */
    private final void addToListIfNameIsUnique(List<NATTModule> modules) throws NonUniqueModuleNamesException {
        for (NATTModule module : modules) {
            if (module.getName().equals(this.name)) {
                throw new NonUniqueModuleNamesException(this.name);
            }
        }
        modules.add(this);
    }

}