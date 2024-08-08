package utb.fai.Core;

import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraktni trida keyword. Zajistuje jednoduche sestaveni testovaci
 * strruktury, kterou je take jednoduse mozne vykonavat. Kazda keyword obsahuje
 * jeji nazev a mapu parametru. Jak je s jednotlivymi parametry uzivano zalezi
 * jiz na tride, ktera od teto abstraknti tridy dedi.
 */
public abstract class Keyword {

    /**
     * Jde o nazev parametru, jaky atribut dostane ve chvili kdy keyword obsahuje
     * jednu jedinou hodnotu, ktere neni nijak pojmenovana. V yaml se jenda
     * napriklad o tento zapis -> keyword_name: "this is parameter value"
     */
    public static final String DEFAULT_PARAMETER_NAME = "default";

    /**
     * Typ hodnoty parametru
     */
    public enum ParameterValueType {
        BOOLEAN,
        LONG,
        DOUBLE,
        STRING,
        LIST,
        KEYWORD,
        NULL
    }

    /**
     * Hodnota parametru. Uchovava informaci o jeho typu a take obsahuje hodnotu
     * samotnou.
     */
    public static class ParameterValue {

        private Object value;
        private ParameterValueType type;

        public ParameterValue(Object value, ParameterValueType type) {
            this.value = value;
            this.type = type;
        }

        public ParameterValueType getType() {
            return type;
        }

        public void setType(ParameterValueType type) {
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
     * Jmeno keywordy. Musi byt vzdy voleno unikatni. Bude vyuzivani pri sestavovani
     * testovaci sada pri vyberu keyword z listu.
     */
    private String keywordName;

    /**
     * Jmeno objektu.
     */
    private String name;

    /**
     * Nactene parametry. Tyto data budou dale zpracovani v potomku teto tridy.
     * Hodnota parametru muze byt: int, float, boolean, String, List nebo Keyword
     */
    private HashMap<String, ParameterValue> parameters;

    /**
     * Rodicovska keyword
     */
    private Keyword parent;

    /**
     * Abstraktni trida keyword
     * 
     * @param keywordName Jmeno keyword
     * @param paramNames  Seznam jmen vsech parametru, ktera tato keyword prebira.
     */
    public Keyword() {
        this.parent = null;
        this.parameters = new HashMap<String, ParameterValue>();

        Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(NATTAnnotation.Keyword.class)) {
            NATTAnnotation.Keyword annotation = clazz.getAnnotation(NATTAnnotation.Keyword.class);
            this.keywordName = annotation.name();
        }
    }

    /**
     * Navrati jmeno objektu
     * 
     * @return Jmeno objektu
     */
    public String getName() {
        return name;
    }

    /**
     * Nastavai nove jmeno objektu
     * 
     * @param name Nove jmeno objektu
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Nastavi jmeno keyword
     * 
     * @param newName Nove jmeno keyword
     */
    protected void setKeywordName(String newName) {
        this.keywordName = newName;
    }

    /**
     * Ziska jmeno keyword
     * 
     * @return Jmeno
     */
    public String getKeywordName() {
        return keywordName;
    }

    /**
     * Ziska referenci na rodicovskou keyword
     * 
     * @return Reference na rodicovskou keyword
     */
    public Keyword getParent() {
        return parent;
    }

    /**
     * Nastave rodice
     * 
     * @param parent Reference na rodice
     */
    public void setParent(Keyword parent) {
        this.parent = parent;
    }

    /**
     * Navrati seznam nactenych parametru
     * 
     * @return Seznam vsech parametru
     */
    public HashMap<String, ParameterValue> getParameters() {
        return parameters;
    }

    /**
     * Vloze do keywordy parametr
     * 
     * @param name  Nazev parametru (neni case sensitive)
     * @param value Libovolna hodnota parametru
     */
    public void putParameter(String name, ParameterValue value) {
        parameters.put(name.toLowerCase(), value);
    }

    /**
     * Overi zda je keyword ignorovana nebo ne. Pokud bude ignorovana, nebude pri
     * testovani vykonavana. Ignorovani je mozne u libovolne keyword nastavit
     * priznakem "ignore: true"
     * 
     * @return
     */
    public boolean isIgnored() {
        ParameterValue val = this.parameters.get("ignore");
        if (val != null) {
            if (val.getType() == ParameterValueType.BOOLEAN) {
                if ((Boolean) val.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Navrati hodnotu parametru keywordy s RAW nactenych hodnot. Jde o bezpecnou
     * metodu. Zastupne symboly za promenne nahradi uz jejich hodnotou (jen ve
     * String).
     * 
     * @param names    Nazev pozadovaneho parametru. Dany parametr muze mit vice
     *                 ruznych nazvu. Je pouzivat hodnota prvniho nelezeneho
     *                 parametru.
     * @param type     Predpokladany typ parametru. Pri nacitani tohoto parametru
     *                 je
     *                 overovano a musi odpovidat.
     * @param required True v pripade kdy je hodnota tohoto parametru vyzadovana.
     *                 Pokud bude false, muze funkce navratit i null v hodnote a
     *                 typ
     *                 parametru bude oznacen jako null, ale exception nebude
     *                 vyvolan.
     * 
     * @return Hodnota parametru
     * @throws InvalidSyntaxInConfigurationException
     */
    public ParameterValue getParameterValue(String[] names, ParameterValueType type, boolean required)
            throws InvalidSyntaxInConfigurationException {

        // ziskani hodnoty parametru podle names, je pouzita prvni nalezena hodnota
        ParameterValue val = null;
        for (String name : names) {
            val = this.parameters.get(name.toLowerCase());
            if (val != null) {
                break;
            }
        }

        // overeni existence hodnoty
        if (val == null) {
            if (!required)
                return new ParameterValue(null, ParameterValueType.NULL);
            throw new InvalidSyntaxInConfigurationException(
                    String.format("Missing parameter '%s' for keyword '%s'!", name, this.getKeywordName()));
        }

        // overeni spravne typu parametru
        if (val.getType() != type) {
            // vyjimka pro typ double x long
            if (type == ParameterValueType.LONG && val.getType() == ParameterValueType.DOUBLE) {
                // musi se jednat o tridu Long ale je Double
                long retyped = ((Double) val.getValue()).longValue();
                val.setValue((Long) retyped);
            } else if (type == ParameterValueType.DOUBLE && val.getType() == ParameterValueType.LONG) {
                // musi se jednat o tridu Double ale je Long
                double retyped = ((Long) val.getValue()).doubleValue();
                val.setValue((Double) retyped);
            } else {
                // pozadovany typ parametru neodpovida tomu ktery je ulozeny v keyworde a ani
                // neni mozna ciselna konverze
                if (!required)
                    return new ParameterValue(null, ParameterValueType.NULL);
                throw new InvalidSyntaxInConfigurationException(
                        String.format("Invalid type of parameter '%s' for keyword '%s'!", name, this.getKeywordName()));
            }
        }

        return val;
    }

    /**
     * Navrati hodnotu parametru keywordy s RAW nactenych hodnot. Jde o bezpecnou
     * metodu. Zastupne symboly za promenne nahradi uz jejich hodnotou (jen ve
     * String).
     * 
     * @param names    Nazev pozadovaneho parametru.
     * @param type     Predpokladany typ parametru. Pri nacitani tohoto parametru
     *                 je
     *                 overovano a musi odpovidat.
     * @param required True v pripade kdy je hodnota tohoto parametru vyzadovana.
     *                 Pokud bude false, muze funkce navratit i null v hodnote a
     *                 typ
     *                 parametru bude oznacen jako null, ale exception nebude
     *                 vyvolan.
     * 
     * @return Hodnota parametru
     * @throws InvalidSyntaxInConfigurationException
     */
    public ParameterValue getParameterValue(String name, ParameterValueType type, boolean required)
            throws InvalidSyntaxInConfigurationException {
        return this.getParameterValue(new String[] {name}, type, required);
    }

    /**
     * Debug funkce pro vypsani struktury keyword na standartni stream
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
                        if (list.get(0) instanceof Keyword) {
                            System.out.printf(line_offset + "- (list) %s: \n", entry.getKey());
                            for (Object item : list) {
                                ((Keyword) item).printToStructure(offset + 4);
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
                    ((Keyword) entry.getValue().getValue()).printToStructure(offset + 4);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Metoda pro sestaveni datove struktury keyword. Nacte pozadovane parametry z
     * predaneho pole s konfigurarci dane keywordy a ulozi si je pro nasledne
     * zpracovani v metode keywordBuild(), ktera musi byt definovane v kazde tride,
     * ktera dedi od tridy Keyword. Kazda keyword muze mit libovolny
     * pocet potomku, jejich objekty s data jsou touto metodou navraceni pro
     * zpracovani v dalsi iteraci
     * 
     * @return Reference na mapu, ktera obsahuje nazev parametru a jeho hodnotu
     * @throws InvalidSyntaxInConfigurationException
     */
    @SuppressWarnings("unchecked")
    public final HashMap<String, ParameterValue> build(Object data) throws InvalidSyntaxInConfigurationException {
        // zde budou navraceny jen koplexni typy, ktere musi byt zpracovany samostatne
        // mimo tuto metodu (keyword nebo list<keyword>)
        HashMap<String, ParameterValue> complexType = new HashMap<>();

        // zpracuje vsechny parametry keywordy. data musi byt nutne typu Map
        if (data instanceof Map) {
            // jedna se o komplexni keyword (obsahuje pouze vice pojmenovanych parametru)
            Map<String, Object> keywordData = (Map<String, Object>) data;
            for (Map.Entry<String, Object> entry : keywordData.entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();

                // jednoduchy typ
                if (paramValue instanceof Boolean) {
                    this.putParameter(paramName, new ParameterValue(
                            (Boolean) paramValue, ParameterValueType.BOOLEAN));

                } else if (paramValue instanceof Integer) {
                    this.putParameter(paramName, new ParameterValue(
                            ((Integer) paramValue).longValue(), ParameterValueType.LONG));

                } else if (paramValue instanceof Long) {
                    this.putParameter(paramName, new ParameterValue(
                            (Long) paramValue, ParameterValueType.LONG));

                } else if (paramValue instanceof String) {
                    this.putParameter(paramName, new ParameterValue(
                            (String) paramValue, ParameterValueType.STRING));

                } else if (paramValue instanceof Float) {
                    this.putParameter(paramName, new ParameterValue(
                            ((Float) paramValue).doubleValue(), ParameterValueType.DOUBLE));

                } else if (paramValue instanceof Double) {
                    this.putParameter(paramName, new ParameterValue(
                            (Double) paramValue, ParameterValueType.DOUBLE));

                    // komplexni typy, ktere vyzaduje dalsi zpracovani
                } else if (paramValue instanceof List) {
                    List<?> list = (List<?>) paramValue;
                    if (list.size() > 0) {
                        if (list.get(0) instanceof Map) {
                            // list keyword (Map object) ... kazda keyworda bude sestavena samostatne mimo
                            // tuto keywordu
                            complexType.put(paramName, new ParameterValue(paramValue, ParameterValueType.LIST));
                        } else {
                            // jedna se o list trivialnich typu (nejedna se o Map objecty) ... z toho duvodu
                            // je mozne zapsat do list parametru teto keywordy
                            this.putParameter(paramName, new ParameterValue(paramValue, ParameterValueType.LIST));
                        }
                    }
                } else {
                    // keyword (potomek) ... bude sestaven mimo tuto keywordu samostane
                    complexType.put(paramName, new ParameterValue(paramValue, ParameterValueType.KEYWORD));
                }
            }
        } else {
            // jedna se o jednoduchou keyword (obsahuje pouze jeden jediny nepojmenovany
            // parametr)
            if (data instanceof Boolean) {
                this.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        (Boolean) data, ParameterValueType.BOOLEAN));

            } else if (data instanceof Integer) {
                this.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        ((Integer) data).longValue(), ParameterValueType.LONG));

            } else if (data instanceof Long) {
                this.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        (Long) data, ParameterValueType.LONG));

            } else if (data instanceof String) {
                this.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        (String) data, ParameterValueType.STRING));

            } else if (data instanceof Float) {
                this.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        ((Float) data).doubleValue(), ParameterValueType.DOUBLE));

            } else if (data instanceof Double) {
                this.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        (Double) data, ParameterValueType.DOUBLE));

            } else if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.size() > 0) {
                    if (list.get(0) instanceof Map) {
                        // list keyword (Map object) ... kazda keyworda bude sestavena samostatne mimo
                        // tuto keywordu
                        complexType.put(Keyword.DEFAULT_PARAMETER_NAME,
                                new ParameterValue(data, ParameterValueType.LIST));
                    } else {
                        // jedna se o list trivialnich typu (nejedna se o Map objecty) ... z toho duvodu
                        // je mozne zapsat do list parametru teto keywordy
                        this.putParameter(Keyword.DEFAULT_PARAMETER_NAME,
                                new ParameterValue(data, ParameterValueType.LIST));
                    }
                }
            }
        }

        return complexType;
    }

    /**
     * V teto metode bude provedena inicializace keywordy. Metoda inicializace je
     * volana az po jejim sestaveni, tedy volani metody "build()".
     * 
     * @throws InvalidSyntaxInConfigurationExceptions
     */
    protected abstract void keywordInit() throws InvalidSyntaxInConfigurationException;

    /**
     * V teto metode bude definice kodu, ktery ma dana keyword vykonat
     * 
     * @return Navrati true v pripade uspesneho provedeni keywordy. Aby test case
     *         byl cely uspesny musi byt vsechny jeho akce v nem provedene uspesne.
     *         Tedy vsechny musi navratit true.
     * 
     * @throws InternalErrorException
     * @throws NonUniqueModuleNamesException
     */
    public abstract boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException;

    /**
     * Jde o akci, ktere bude volana na konci "zivotniho cyklu keywordu". Tedy ve
     * chvili kdy v danem kontextu, ve kterem byla vytvorena, uz dale byt vyuzita
     * byt nemuzes v dusledku konce samotneho kontextu. Metoda by mela obsahovat
     * akce, ktere ukonci pripadne nejake vnitrne spustene moduly nebo vykona
     * ukoncujici akce.
     * 
     * @throws TestedAppFailedToRunException
     * @throws InternalErrorException
     */
    public abstract void deleteAction() throws InternalErrorException;

    /**
     * Navrati popis keywordy. Jeji nazev a jeji parametry + pripadne duvod selhani
     * 
     * @return Popis keyword
     */
    public String getDescription() {
        // jmeno keyword
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

        // parametry
        List<String> paramStr = new ArrayList<String>();
        for (Map.Entry<String, ParameterValue> param : this.parameters.entrySet()) {
            if (param.getValue().getType() != ParameterValueType.LIST
                    && param.getValue().getType() != ParameterValueType.KEYWORD) {
                if (param.getKey().equals(Keyword.DEFAULT_PARAMETER_NAME)) {
                    paramStr.add(String.format("%s", param.getValue().getValue()));
                } else {
                    if (param.getValue().getType() == ParameterValueType.STRING) {
                        // jde o string
                        String value = (String) param.getValue().getValue();
                        // omezeni hodnoty atributu na max 70 znaku
                        if (value.length() > 70) {
                            value = value.substring(0, 67) + "..";
                        }
                        // symboli < a > nahradi jinym formatem aby je bylo mozne spolehlice zobrazit v
                        // html
                        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                        paramStr.add(String.format("%s = \"%s\"", param.getKey(), value));
                    } else {
                        // nejde o string
                        paramStr.add(String.format("%s = %s", param.getKey(), param.getValue().getValue()));
                    }
                }
            }
        }
        String parameters = String.join(", ", paramStr);

        return "<b>[" + name + "]</b> " + parameters;
    }

}
