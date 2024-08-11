package utb.fai.natt.spi;

import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for a keyword. It ensures easy construction of a test
 * structure
 * that can also be easily executed. Each keyword contains its name and a map of
 * parameters.
 * How each parameter is used depends on the class that inherits from this
 * abstract class.
 */
public abstract class NATTKeyword {

    /**
     * This is the parameter name that a keyword gets when it contains a single
     * unnamed value. For example, in YAML it would be something like ->
     * keyword_name: "this is parameter value"
     */
    public static final String DEFAULT_PARAMETER_NAME = "default";

    /**
     * Type of parameter value.
     */
    public enum ParamValType {
        BOOLEAN,
        LONG,
        DOUBLE,
        STRING,
        LIST,
        KEYWORD,
        NULL
    }

    /**
     * Parameter value. Stores information about its type and also contains the
     * actual value.
     */
    public static class ParameterValue {

        private Object value;
        private ParamValType type;

        public ParameterValue(Object value, ParamValType type) {
            this.value = value;
            this.type = type;
        }

        public ParamValType getType() {
            return type;
        }

        public void setType(ParamValType type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    /**
     * Documentation of a keyword
     */
    public static class KeywordDocumentation {

        // Name of the keyword
        public String name;

        // Description of the keyword
        public String description;

        // List of parameters
        public String[] parameters;

        // List of types of parameters
        public ParamValType[] types;

        // Keyword group
        public String kwGroup;

    }

    /**
     * Documentation of the keyword. Holds information about the keyword (name,
     * description, parameters, and parameter types).
     */
    private KeywordDocumentation documentation;

    /**
     * Name of the object/keyword.
     */
    private String name;

    /**
     * Loaded parameters. These data will be further processed in the subclass of
     * this class.
     * The parameter value can be: int, float, boolean, String, List, or Keyword.
     */
    private HashMap<String, ParameterValue> parameters;

    /**
     * Parent keyword.
     */
    private NATTKeyword parent;

    /**
     * Abstract NATT keyword class.
     * 
     * @param keywordName Name of the keyword.
     * @param paramNames  List of all parameter names that this keyword takes.
     */
    public NATTKeyword() {
        this.parent = null;
        this.documentation = new KeywordDocumentation();
        this.parameters = new HashMap<String, ParameterValue>();

        // setup the key documentation
        Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(NATTAnnotation.Keyword.class)) {
            NATTAnnotation.Keyword annotation = clazz.getAnnotation(NATTAnnotation.Keyword.class);
            this.documentation.name = annotation.name();
            this.documentation.description = annotation.description();
            this.documentation.parameters = annotation.parameters();
            this.documentation.types = annotation.types();
            this.documentation.kwGroup = annotation.kwGroup();
        }
    }

    /**
     * Returns the name of the object.
     * 
     * @return Name of the object.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name for the object.
     * 
     * @param name New name of the object.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the keyword name.
     * 
     * @param newName New name of the keyword.
     */
    protected void setKeywordName(String newName) {
        this.documentation.name = newName;
    }

    /**
     * Gets the keyword name.
     * 
     * @return Name of the keyword.
     */
    public String getKeywordName() {
        return documentation.name;
    }

    /**
     * Gets a reference to the parent keyword.
     * 
     * @return Reference to the parent keyword.
     */
    public NATTKeyword getParent() {
        return parent;
    }

    /**
     * Sets the parent keyword.
     * 
     * @param parent Reference to the parent keyword.
     */
    public void setParent(NATTKeyword parent) {
        this.parent = parent;
    }

    /**
     * Returns the list of loaded parameters.
     * 
     * @return List of all parameters.
     */
    public HashMap<String, ParameterValue> getParameters() {
        return parameters;
    }

    /**
     * Adds a parameter to the keyword.
     * 
     * @param name  Name of the parameter (case insensitive).
     * @param value Any value of the parameter.
     */
    public void putParameter(String name, ParameterValue value) {
        parameters.put(name.toLowerCase(), value);
    }

    /**
     * Checks whether the keyword is ignored or not. If it is ignored, it will not
     * be executed during testing.
     * Ignoring can be set for any keyword with the flag "ignore: true".
     * 
     * @return true if the keyword is ignored, false otherwise.
     */
    public boolean isIgnored() {
        ParameterValue val = this.parameters.get("ignore");
        if (val != null) {
            if (val.getType() == ParamValType.BOOLEAN) {
                if ((Boolean) val.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the value of the keyword's parameter with RAW loaded values. This is
     * a safe method.
     * Placeholder symbols for variables are replaced with their values (only in
     * String).
     * 
     * @param names    Name of the requested parameter. The parameter may have
     *                 multiple names. The value of the first found parameter is
     *                 used.
     * @param type     Expected type of the parameter. It is validated during
     *                 loading and must match.
     * @param required True if the value of this parameter is required. If false,
     *                 the function may return null and the parameter type will be
     *                 marked as null, but no exception will be thrown.
     * 
     * @return Value of the parameter.
     * @throws InvalidSyntaxInConfigurationException if the parameter is missing or
     *                                               has an invalid type.
     */
    public ParameterValue getParameterValue(String[] names, ParamValType type, boolean required)
            throws InvalidSyntaxInConfigurationException {

        // Retrieve the parameter value based on names, using the first found value.
        ParameterValue val = null;
        String paramName = "";
        for (String name : names) {
            val = this.parameters.get(name.toLowerCase());
            paramName = name.toLowerCase();
            if (val != null) {
                break;
            }
        }

        // Check for the existence of the value.
        if (val == null) {
            if (!required)
                return new ParameterValue(null, ParamValType.NULL);
            throw new InvalidSyntaxInConfigurationException(
                    String.format("Missing parameter '%s' for keyword '%s'!", paramName, this.getKeywordName()));
        }

        // Check the correct type of the parameter.
        if (val.getType() != type) {
            // Exception for double x long type.
            if (type == ParamValType.LONG && val.getType() == ParamValType.DOUBLE) {
                // It should be of Long class but is Double.
                long retyped = ((Double) val.getValue()).longValue();
                val.setValue((Long) retyped);
            } else if (type == ParamValType.DOUBLE && val.getType() == ParamValType.LONG) {
                // It should be of Double class but is Long.
                double retyped = ((Long) val.getValue()).doubleValue();
                val.setValue((Double) retyped);
            } else {
                // The expected type of the parameter does not match the type stored in the
                // keyword and there is no numeric conversion possible.
                if (!required)
                    return new ParameterValue(null, ParamValType.NULL);
                throw new InvalidSyntaxInConfigurationException(
                        String.format("Invalid type of parameter '%s' for keyword '%s'!", paramName,
                                this.getKeywordName()));
            }
        }

        return val;
    }

    /**
     * Returns the value of the keyword's parameter with RAW loaded values. This is
     * a safe method.
     * Placeholder symbols for variables are replaced with their values (only in
     * String).
     * 
     * @param names    Name of the requested parameter.
     * @param type     Expected type of the parameter. It is validated during
     *                 loading and must match.
     * @param required True if the value of this parameter is required. If false,
     *                 the function may return null and the parameter type will be
     *                 marked as null, but no exception will be thrown.
     * 
     * @return Value of the parameter.
     * @throws InvalidSyntaxInConfigurationException if the parameter is missing or
     *                                               has an invalid type.
     */
    public ParameterValue getParameterValue(String name, ParamValType type, boolean required)
            throws InvalidSyntaxInConfigurationException {
        return this.getParameterValue(new String[] { name }, type, required);
    }

    /**
     * Debug function to print the structure of the keyword to the standard stream.
     * 
     * @param offset Offset for printing (number of spaces before the printout).
     */
    public void printToStructure(int offset) {
        String line_offset = String.valueOf(" ").repeat(Math.max(0, offset));
        System.out.printf(line_offset + "(Keyword) [%s]:\n", this.getClass().toString());
        for (Map.Entry<String, ParameterValue> entry : this.parameters.entrySet()) {
            switch (entry.getValue().getType()) {
                case BOOLEAN:
                    System.out.printf(line_offset + "(boolean) %s:%s\n", entry.getKey(),
                            ((boolean) entry.getValue().getValue()) ? "true" : "false");
                    break;
                case LONG:
                    System.out.printf(line_offset + "- (long) %s: %d\n", entry.getKey(), entry.getValue().getValue());
                    break;
                case STRING:
                    System.out.printf(line_offset + "- (string) %s: %s\n", entry.getKey(), entry.getValue().getValue());
                    break;
                case DOUBLE:
                    System.out.printf(line_offset + "- (float) %s: %f\n", entry.getKey(), entry.getValue().getValue());
                    break;
                case LIST:
                    List<?> list = (List<?>) entry.getValue().getValue();
                    if (list.size() > 0) {
                        if (list.get(0) instanceof NATTKeyword) {
                            System.out.printf(line_offset + "- (list) %s: \n", entry.getKey());
                            for (Object item : list) {
                                ((NATTKeyword) item).printToStructure(offset + 4);
                            }
                        } else {
                            StringBuilder listString = new StringBuilder();
                            for (Object item : list) {
                                listString.append(item.toString()).append(",");
                            }
                            if (listString.length() > 0) {
                                listString.deleteCharAt(listString.length() - 1);
                            }
                            System.out.printf(line_offset + "- (list) %s: [%s]\n", entry.getKey(),
                                    listString.toString());
                        }
                    }
                    break;
                case KEYWORD:
                    ((NATTKeyword) entry.getValue().getValue()).printToStructure(offset + 4);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * In this method, the initialization of the keyword will be performed. The
     * initialization method is
     * called only after its construction, i.e., after calling the "build()" method.
     * 
     * @throws InvalidSyntaxInConfigurationException if initialization fails due to
     *                                               invalid syntax.
     */
    public abstract void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException;

    /**
     * In this method, the code that the keyword should execute will be defined.
     * 
     * @return Returns true if the execution of the keyword was successful. For the
     *         test case to be
     *         completely successful, all its actions must be successful, meaning
     *         they must all return true.
     * 
     * @throws InternalErrorException        if an internal error occurs during
     *                                       execution.
     * @throws NonUniqueModuleNamesException if module names are not unique.
     */
    public abstract boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException;

    /**
     * This is an action that will be called at the end of the "lifecycle" of the
     * keyword.
     * That is, when it can no longer be used in the context in which it was created
     * due to the end of that context.
     * The method should contain actions that end any internally started modules or
     * perform cleanup actions.
     * 
     * @throws InternalErrorException if an internal error occurs during cleanup.
     */
    public abstract void deleteAction(INATTContext ctx) throws InternalErrorException;

    /**
     * Returns a description of the keyword. Its name and parameters and status
     * messages (success, error, etc.).
     * 
     * @return Description of the keyword.
     */
    public String getDescription() {
        // keyword name
        String[] words = this.getKeywordName().split("_");
        StringBuilder outputBuilder = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (outputBuilder.length() > 0) {
                    outputBuilder.append(" ");
                }
                outputBuilder.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
        }
        String name = outputBuilder.toString();

        // parameters
        List<String> paramStr = new ArrayList<String>();
        for (Map.Entry<String, ParameterValue> param : this.parameters.entrySet()) {
            if (param.getValue().getType() != ParamValType.LIST
                    && param.getValue().getType() != ParamValType.KEYWORD) {
                if (param.getKey().equals(NATTKeyword.DEFAULT_PARAMETER_NAME)) {
                    // only one parameter
                    if (param.getValue().getType() == ParamValType.STRING) {
                        paramStr.add(String.format("\"%s\"", param.getValue().getValue()));
                    } else {
                        paramStr.add(String.format("%s", param.getValue().getValue()));
                    }
                } else {
                    if (param.getValue().getType() == ParamValType.STRING) {
                        // it is a string
                        String value = (String) param.getValue().getValue();
                        // limit attribute value to a maximum of 70 characters
                        if (value.length() > 70) {
                            value = value.substring(0, 67) + "..";
                        }
                        // replace < and > symbols with a format that can be reliably displayed in HTML
                        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                        paramStr.add(String.format("%s = \"%s\"", param.getKey(), value));
                    } else {
                        // it is not a string
                        paramStr.add(String.format("%s = %s", param.getKey(), param.getValue().getValue()));
                    }
                }
            }
        }
        String parameters = String.join(", ", paramStr);

        return "<b>[" + name + "]</b> " + parameters;
    }

    /**
     * Returns documentation for the keyword. Returns null if the keyword does not
     * have any documentation.
     */
    public KeywordDocumentation getDocumentation() {
        return this.documentation;
    }

}