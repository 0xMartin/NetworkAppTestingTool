package utb.fai.Core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utb.fai.Core.Keyword.ParameterValue;
import utb.fai.Core.Keyword.ParameterValueType;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueTestNamesException;
import utb.fai.Keyword.Main.TestCaseKw;
import utb.fai.Keyword.Main.TestSuiteKw;

/**
 * Tato trida zajistuje vygenerovani struktury trid "keyword" v programu, ktere
 * jsou uz uzpusobeny primo pro vykonani. Kazdy keyword ma libovolne mnozstvi
 * parametru. Hodnotou parametru muze byt jednoduchy typ jako je naprikla long,
 * string, ... ale take komplexni, kterym je bude dalsi keyword "potomek" nebo
 * list libovolnych prvku stejneho typu
 */
public class NATTTestBuilder {

    /**
     * Zaznam do fronty pro pozdejsi sestaveni keywordy. Obsahuje referenci na
     * rodice, nazev atributu a take hodnotu samotneho objektu pro zpracovani. Zde
     * hodnotou bude vzdy jen komplexni typ, ktery vyzaduje dalsi zpracovani
     * (keyword nebo list keyword).
     */
    private static class BuildQueueEntry {
        public Keyword parentKeyword;
        public String parameterName;
        public Keyword.ParameterValue parameterValue;

        public BuildQueueEntry(Keyword parentKeyword, String parameterName, Keyword.ParameterValue parameterValue) {
            this.parentKeyword = parentKeyword;
            this.parameterName = parameterName;
            this.parameterValue = parameterValue;
        }
    }

    /**
     * Sestavi testovaci strukturu podle predanych konfiguracnich dat. Konfiguracni
     * data obsahuji data primo nactene z yaml souboru.
     * 
     * @param configurationData Data nactene z yaml souboru, ktere definuji
     *                          testovani
     * @return Reference na root keyword, u ktere se zapocne s vykonavanim testu
     * @throws InvalidSyntaxInConfigurationException
     */
    @SuppressWarnings("unchecked")
    public static Keyword buildTests(Map<String, Object> configurationData)
            throws InvalidSyntaxInConfigurationException, InternalErrorException {

        // fronta elementu pro zpracovani
        LinkedList<BuildQueueEntry> buildQueue = new LinkedList<>();

        // vlozi prvotni root element do fronty (data vlozna do fronty vzdy obsahuji bud
        // keyword nebo list keyword. data samotne keyword at uz jedine tridy nebo i
        // jako prvek v listu je vzdy typu Map<String, Object> kdy obsahuje vzdy jeden
        // zaznam .. klic:hodnota ... klicem je vzdy nazev samotne keyword a hodnota uz
        // jsou jeji parametry, ktere se vkladaji do metody Keyword.build())
        buildQueue.add(new BuildQueueEntry(null, "",
                new Keyword.ParameterValue(configurationData, Keyword.ParameterValueType.KEYWORD)));

        // postupne sestaveni vsech ve fronte
        Keyword root = null;
        while (!buildQueue.isEmpty()) {
            // zaznam ve fronte
            BuildQueueEntry entry = buildQueue.pollFirst();

            // ziska datovou hodnotu urcenou pro zpracovani
            Keyword.ParameterValue value = entry.parameterValue;

            if (value.getType() == Keyword.ParameterValueType.KEYWORD) {
                // format dat <Map>: nazev_keywordy: {atributy ...}
                Map<String, Object> data = (Map<String, Object>) value.getValue();
                // zpracovani keyword
                Keyword keywordChild = processKeyword(buildQueue, entry, data);
                // ulozi root element
                if (root == null) {
                    root = keywordChild;
                }
                // vlozeni pomtomka do rodice
                if (entry.parentKeyword != null) {
                    keywordChild.setParent(entry.parentKeyword);
                    entry.parentKeyword.putParameter(entry.parameterName,
                            new Keyword.ParameterValue(keywordChild, Keyword.ParameterValueType.KEYWORD));
                }

            } else if (value.getType() == Keyword.ParameterValueType.LIST) {
                LinkedList<Keyword> keywordList = new LinkedList<Keyword>();

                List<Object> elementsData = (List<Object>) value.getValue();
                for (Object element : elementsData) {
                    // format dat v jedno elementu listu <Map>: nazev_keywordy: {atributy ...}
                    Map<String, Object> data = (Map<String, Object>) element;
                    // zpracovani keyword
                    Keyword keywordChild = processKeyword(buildQueue, entry, data);
                    // ulozi root element
                    if (root == null) {
                        root = keywordChild;
                    }
                    // vlozeni do listu
                    keywordList.add(keywordChild);
                    // prirazeni rodice
                    if (entry.parentKeyword != null) {
                        keywordChild.setParent(entry.parentKeyword);
                    }
                }

                // vlozeni listu keyword do rodice
                if (entry.parentKeyword != null) {
                    entry.parentKeyword.putParameter(entry.parameterName,
                            new Keyword.ParameterValue(keywordList, Keyword.ParameterValueType.LIST));
                }
            }

        }

        return root;
    }

    /**
     * Metoda pro zpracovani jedne keyword. Je vyuzivana jak pro zpracovani
     * samostatne tridy tak pro zpracovani vice tridy v listu.
     * 
     * @param buildQueue Fronta z dalsimi data pro sestaveni. Tato metoda zde na
     *                   tuto frontu jen muze pridavat dalsi zaznamy.
     * @param entry      Jeden prvek fronty, ktery bude touto metodou zpracovan
     * @param root       Root keyword. Je pocatecni keyworda, ktera je rodicem pro
     *                   vsechny ostatni a taky u ni zacina vykonavani.
     * @param data       Data keywordy. Format dat je nasledujici <Map>:
     *                   nazev_keywordy:
     *                   {atributy keyword ...}
     * 
     * @return Vysledna keyworda
     * 
     * @throws InvalidSyntaxInConfigurationException
     * @throws InternalErrorException
     */
    private static Keyword processKeyword(LinkedList<BuildQueueEntry> buildQueue, BuildQueueEntry entry,
            Map<String, Object> data) throws InvalidSyntaxInConfigurationException, InternalErrorException {
        // ziskani jmena keyword
        Set<String> keys = data.keySet();
        String keywordName = null;
        if (!keys.isEmpty()) {
            keywordName = keys.iterator().next();
        } else {
            throw new InvalidSyntaxInConfigurationException(
                    String.format("Missing name of some keyword in keyword [%s] for parameter [%s]",
                            entry.parentKeyword != null ? entry.parentKeyword.getKeywordName() : "<root of file>",
                            entry.parameterName));
        }

        // vytvori instanci keywordy
        Keyword keywordChild = createKeywordInstance(keywordName);

        // sestaveni keyword
        HashMap<String, Keyword.ParameterValue> complexTypes = NATTTestBuilder.buildKeyword(keywordChild,
                data.get(keywordName));

        // vlozeni jejich potomku do fronty pro dalsi zpracovani
        for (Map.Entry<String, Keyword.ParameterValue> e : complexTypes.entrySet()) {
            buildQueue.add(new BuildQueueEntry(keywordChild, e.getKey(), e.getValue()));
        }

        return keywordChild;
    }

    /**
     * Metoda pro sestaveni datove struktury keyword. Nacte pozadovane parametry z
     * predaneho pole s konfigurarci dane keywordy a ulozi si je pro nasledne
     * zpracovani v metode keywordBuild(), ktera musi byt definovane v kazde tride,
     * ktera dedi od tridy Keyword. Kazda keyword muze mit libovolny
     * pocet potomku, jejich objekty s data jsou touto metodou navraceni pro
     * zpracovani v dalsi iteraci
     * 
     * @param keyword Keyword, do ktere se maji nacist data s konfiguracniho objektu
     * @param data    Konfiguracni data keywordy.
     * 
     * @return Reference na mapu, ktera obsahuje nazev parametru a jeho hodnotu
     * @throws InvalidSyntaxInConfigurationException
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, ParameterValue> buildKeyword(Keyword keyword, Object data)
            throws InvalidSyntaxInConfigurationException {
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
                    keyword.putParameter(paramName, new ParameterValue(
                            (Boolean) paramValue, ParameterValueType.BOOLEAN));

                } else if (paramValue instanceof Integer) {
                    keyword.putParameter(paramName, new ParameterValue(
                            ((Integer) paramValue).longValue(), ParameterValueType.LONG));

                } else if (paramValue instanceof Long) {
                    keyword.putParameter(paramName, new ParameterValue(
                            (Long) paramValue, ParameterValueType.LONG));

                } else if (paramValue instanceof String) {
                    keyword.putParameter(paramName, new ParameterValue(
                            (String) paramValue, ParameterValueType.STRING));

                } else if (paramValue instanceof Float) {
                    keyword.putParameter(paramName, new ParameterValue(
                            ((Float) paramValue).doubleValue(), ParameterValueType.DOUBLE));

                } else if (paramValue instanceof Double) {
                    keyword.putParameter(paramName, new ParameterValue(
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
                            keyword.putParameter(paramName, new ParameterValue(paramValue, ParameterValueType.LIST));
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
                keyword.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        (Boolean) data, ParameterValueType.BOOLEAN));

            } else if (data instanceof Integer) {
                keyword.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        ((Integer) data).longValue(), ParameterValueType.LONG));

            } else if (data instanceof Long) {
                keyword.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        (Long) data, ParameterValueType.LONG));

            } else if (data instanceof String) {
                keyword.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        (String) data, ParameterValueType.STRING));

            } else if (data instanceof Float) {
                keyword.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
                        ((Float) data).doubleValue(), ParameterValueType.DOUBLE));

            } else if (data instanceof Double) {
                keyword.putParameter(Keyword.DEFAULT_PARAMETER_NAME, new ParameterValue(
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
                        keyword.putParameter(Keyword.DEFAULT_PARAMETER_NAME,
                                new ParameterValue(data, ParameterValueType.LIST));
                    }
                }
            }
        }

        return complexType;
    }

    /**
     * Vytvori instaceni keyword podle daneho jmena
     * 
     * @param keywordName Jmeno keyword
     * @return Vytvorena instance
     * @throws InvalidSyntaxInConfigurationException
     * @throws InternalErrorException
     */
    private static Keyword createKeywordInstance(String keywordName)
            throws InvalidSyntaxInConfigurationException, InternalErrorException {

        // ziska tridu kokretni keyword podle jejiho syntatickeho nazvu
        Class<?> keywordClass = NATTContext.instance().getKeywordSet().get(keywordName);
        if (keywordClass == null) {
            throw new InvalidSyntaxInConfigurationException(
                    String.format("Keyword with name '%s' not found!", keywordName));
        }

        // vytvori instanci tridy dane keyword podle dane tridy
        Keyword keyword = null;
        try {
            keyword = (Keyword) keywordClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new InternalErrorException("Failed to create instance of keyword [" +
                    keywordClass + "]");
        }

        return keyword;

    }

    /**
     * Inicializuje celou testovaci strukturu
     * 
     * @param rootKeyword Reference na root keyword
     * @throws InvalidSyntaxInConfigurationException
     * @throws NonUniqueTestNamesException
     */
    public static void initTestScructure(Keyword rootKeyword)
            throws InvalidSyntaxInConfigurationException, NonUniqueTestNamesException {
        LinkedList<String> testNames = new LinkedList<String>();
        LinkedList<Keyword> initQueue = new LinkedList<Keyword>();
        initQueue.add(rootKeyword);
        while (!initQueue.isEmpty()) {
            // ziska keyword z inicializacni fronty
            Keyword keyword = initQueue.pollFirst();
            if (keyword == null)
                continue;

            // inicializuje frontu
            keyword.keywordInit();

            // test unikatnosti nazvu testovaciho pripadu / sady
            if (keyword instanceof TestCaseKw || keyword instanceof TestSuiteKw) {
                if (testNames.indexOf(keyword.getName()) != -1) {
                    throw new NonUniqueTestNamesException(keyword.getName());
                }
                testNames.add(keyword.getName());
            }

            // ziska vsechny parametry keywordy
            for (Map.Entry<String, Keyword.ParameterValue> entry : keyword.getParameters().entrySet()) {
                if (entry.getValue().getType() == ParameterValueType.KEYWORD) {
                    // obsahem parametru je dalsi keyword
                    initQueue.add((Keyword) entry.getValue().getValue());

                } else if (entry.getValue().getType() == ParameterValueType.LIST) {
                    // obsahem parametru je list ... z listu vlozi do fronty vsechny ty objekty,
                    // ktere jsou typu keyword
                    List<?> list = (List<?>) entry.getValue().getValue();
                    if (!list.isEmpty()) {
                        for (Object obj : list) {
                            if (obj instanceof Keyword) {
                                initQueue.add((Keyword) obj);
                            }
                        }
                    }

                }
            }
        }
    }

}
