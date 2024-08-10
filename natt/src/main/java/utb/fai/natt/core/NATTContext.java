package utb.fai.natt.core;

import com.aventstack.extentreports.ExtentReports;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.keyword.AppControll.*;
import utb.fai.natt.keyword.Assert.*;
import utb.fai.natt.keyword.General.*;
import utb.fai.natt.keyword.Main.*;
import utb.fai.natt.keyword.Module.*;
import utb.fai.natt.reportGenerator.TestCaseResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kontext testovani. Jedna se o singleton tridu. Obsahuje vsechny dulezite
 * komponenty, ktere jsou sdileny v ramci celeho tohoto testovaciho nastroje.
 */
public class NATTContext implements INATTContext {

    private static NATTContext cntx;

    private NATTLogger logger = new NATTLogger(NATTContext.class);

    /**********************************************************************************************/
    // Trida ExtentReports, ktera je vyzadovana pro generovani reportu o provedenych
    // testech v ramci tohoto nastroje (musi byt nastavena externe)
    private ExtentReports reportExtent;

    // list vsech vysledku testovacich pripadu
    private LinkedList<TestCaseResult> testCaseResults;

    // vysledne bodove hodnoceni
    private double finalScore;

    // maximalni mozne bodove hodnoceni za testovani aplikace
    private double maxScore;

    // seznam aktualne aktivnich modulu
    private LinkedList<NATTModule> modules;

    // obsahuje vsechny zprava ze vsech modulu a standartniho streamu externe
    // testovane aplikace
    private MessageBuffer messageBuffer;

    // vsechny promenne vyuzivane pri testovani
    private ConcurrentHashMap<String, String> variables;
    private Stack<ConcurrentHashMap<String, String>> variableHistory;

    // obsahuje vsechny registrovane custom keywordy
    private HashMap<String, CustomKeywordKw> customKeywords;

    // obsahuje vsechny registrovane nazvy keyword a k nim jejich prislusne tridy
    private final HashMap<String, Class<?>> keywordSet;

    /**********************************************************************************************/

    /**
     * Navrati referenci list aktivnich modulu
     * 
     * @return List aktivnich modulu
     */
    public LinkedList<NATTModule> getModules() {
        return modules;
    }

    /**
     * Bezpecne ziskani reference na dany modul.
     * 
     * @param name Nazev hledaneho modulu
     * @return Reference na modul
     */
    public NATTModule getModule(String name) {
        Optional<NATTModule> moduleOptional = this.modules.stream().filter(m -> m.getName().equals(name)).findFirst();
        if (moduleOptional.isPresent()) {
            NATTModule module = moduleOptional.get();
            return module;
        } else {
            return null;
        }
    }

    /**********************************************************************************************/

    /**
     * Navrati tridu obsahujici buffery zprav pro vsechny moduly
     * 
     * @return MessageBuffer
     */
    public MessageBuffer getMessageBuffer() {
        return messageBuffer;
    }

    /**********************************************************************************************/

    /**
     * Navrati vsechny vytvorene promenne
     * 
     * @return Mapa promennych
     */
    public ConcurrentHashMap<String, String> getVariables() {
        return variables;
    }

    /**
     * Odstrani vsechny promenne
     */
    public void clearVariables() {
        if (this.variables != null) {
            this.variables.clear();
        }
    }

    /**
     * Ziska hodnu jedne specificke promenne
     * 
     * @param name Jmeno promenne
     * @return Hodnota promenne
     */
    public String getVariable(String name) {
        return this.variables.get(name);
    }

    /**
     * Ulozi hodnotu do promenne
     * 
     * @param name  Jmeno promenne. Musi byt jedno slove bez mezer. Pripadne mezery
     *              budou nahrazeny "_"
     * @param value Hodnota promenne. Muze byt prazdna nebo i null. Null bude
     *              nahrazeno prazdnym retezcem.
     * 
     * @return V pripade uspesneho vytvoreni promenne navrati jeji nazev v opacnem
     *         pripade navrati null
     */
    public String storeValueToVariable(String name, String value) {
        if (name == null) {
            return null;
        }
        if (name.isEmpty()) {
            return null;
        }
        if (value == null) {
            value = "";
        }

        name = name.replace(" ", "_");
        this.variables.put(name, value);
        logger.info(String.format("Data has been stored in to the variable '%s'. Data value: %s", name, value));
        return name;
    }

    /**
     * Ulozi aktualni stav promennych do historie pro pro nasledne obnoveni tohoto
     * stavu
     */
    public void saveVariablesState() {
        ConcurrentHashMap<String, String> snapshot = new ConcurrentHashMap<>(variables);
        variableHistory.push(snapshot);
        logger.info("Variables state has been saved.");
    }

    /**
     * Obnoveni promennych na hodnotu ulozenou v historii
     */
    public void restoreVariablesState() {
        if (!variableHistory.isEmpty()) {
            variables = variableHistory.pop();
            logger.info("Variables state has been restored.");
        } else {
            logger.warning("No saved state to restore.");
        }
    }

    /**********************************************************************************************/

    /**
     * Navrati referenci na list obsahujici vsechny registrovane custom keywordy
     * 
     * @return HashMap<String, CustomKeywordKw>
     */
    public HashMap<String, CustomKeywordKw> getCustomKeywords() {
        return this.customKeywords;
    }

    /**
     * Vymaze vsechny custom keywordy
     */
    public void clearCustomKeywords() {
        this.customKeywords.clear();
    }

    /**********************************************************************************************/

    /**
     * Ziska vysledky vsech probhlich testovacich pripadu
     * 
     * @return List vysledku testovacich pripadu
     */
    public List<TestCaseResult> getTestCaseResults() {
        return this.testCaseResults;
    }

    /**
     * Vysledek testovaciho pripadu priradi do listu vsech vysledku.
     * 
     * @param res Reference na vyslede testovaciho pripadu
     */
    public void bindTestCaseResult(TestCaseResult res) {
        if (res != null) {
            this.testCaseResults.add(res);
        }
    }

    /**********************************************************************************************/

    /**
     * Ziska sadu vsech keyword, ktere jsou dostupne pro tento nastroj
     * 
     * @return Sada keyword
     */
    public HashMap<String, Class<?>> getKeywordSet() {
        return keywordSet;
    }

    /**
     * Ziska referenci na ExtentReports, ktery je nezbytny pro generovani reportu
     * 
     * @return ExtentReports
     */
    public ExtentReports getReportExtent() {
        return reportExtent;
    }

    /**
     * Nastavi ExtentReports, ktery je vyuzivan pro generovani reportu
     * 
     * @param reportExtent Reference na ExtentReports
     */
    public void setReportExtent(ExtentReports reportExtent) {
        this.reportExtent = reportExtent;
    }

    /**
     * Ziska finalni score/hodnoceni testovane aplikace.
     * 
     * @return finalni score
     */
    public double getFinalScore() {
        return finalScore;
    }

    /**
     * Nastavi finalni score/hodnoceni testovane aplikace
     * 
     * @param resultPoints finalni score
     */
    public void setFinalScore(double resultPoints) {
        this.finalScore = resultPoints;
    }

    /**
     * Ziska maximalni mozne dosazitelne score/hodnoceni pri testovani
     * 
     * @return Maximalni hodnoceni
     */
    public double getMaxScore() {
        return maxScore;
    }

    /**
     * Nastavi maximalni mozne dosazitelne score/hodnoceni pri testovani
     * 
     * @param maxScore Maximalni hodnoceni
     */
    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    /**********************************************************************************************/

    private NATTContext() {
        this.finalScore = 100.0;
        this.maxScore = 100.0;
        this.reportExtent = null;
        this.modules = new LinkedList<NATTModule>();
        this.testCaseResults = new LinkedList<TestCaseResult>();
        this.messageBuffer = new MessageBuffer();
        this.variables = new ConcurrentHashMap<String, String>();
        this.variableHistory = new Stack<ConcurrentHashMap<String, String>>();
        this.customKeywords = new HashMap<String, CustomKeywordKw>();

        // keyword list /////////////////////////////////////////////////////
        this.keywordSet = new HashMap<String, Class<?>>();
        // main
        this.keywordSet.put("test_root", TestRootKw.class);
        this.keywordSet.put("test_suite", TestSuiteKw.class);
        this.keywordSet.put("test_case", TestCaseKw.class);
        // general
        this.keywordSet.put("clear_buffer", ClearBufferKw.class);
        this.keywordSet.put("count_and_store", CountAndStoreKw.class);
        this.keywordSet.put("read_file", ReadFileKw.class);
        this.keywordSet.put("store_to_var", StoreToVarKw.class);
        this.keywordSet.put("wait", WaitKw.class);
        this.keywordSet.put("wait_until", WaitUntilKw.class);
        this.keywordSet.put("json_get", JsonGetKw.class);
        this.keywordSet.put("buffer_get", BufferGetKw.class);
        this.keywordSet.put("write_file", WriteFileKw.class);
        this.keywordSet.put("set_var", SetVarKw.class);
        this.keywordSet.put("replace", ReplaceKw.class);
        this.keywordSet.put("read_net_file", ReadNetFileKw.class);
        this.keywordSet.put("write_net_file", WriteNetFileKw.class);
        this.keywordSet.put("custom_keyword", CustomKeywordKw.class);
        this.keywordSet.put("call_keyword", CallKeywordKw.class);
        // assert
        this.keywordSet.put("assert_larger", AssertLargerKw.class);
        this.keywordSet.put("assert_lower", AssertLowerKw.class);
        this.keywordSet.put("assert_string", AssertStringKw.class);
        this.keywordSet.put("assert_equals", AssertEqualsKw.class);
        this.keywordSet.put("assert_range", AssertRangeKw.class);
        this.keywordSet.put("assert_app_is_running", AssertAppIsRunningKw.class);
        this.keywordSet.put("assert_module_is_running", AssertModuleIsRunningKw.class);
        this.keywordSet.put("assert_json", AssertJsonKw.class);
        // app controll
        this.keywordSet.put("run_app", RunAppKw.class);
        this.keywordSet.put("run_app_later", RunAppLaterKw.class);
        this.keywordSet.put("reload_app", ReloadAppKw.class);
        this.keywordSet.put("standard_stream_send", StandardStreamSendKw.class);
        // module
        this.keywordSet.put("create_filter_action", CreateFilterActionKw.class);
        this.keywordSet.put("clear_filter_actions", ClearFilterActionsKw.class);
        this.keywordSet.put("module_send", ModuleSendKw.class);
        this.keywordSet.put("termite_module", TermiteModuleKw.class);
        // create module
        this.keywordSet.put("create_email_server", CreateEmailServerKw.class);
        this.keywordSet.put("create_mqtt_broker", CreateMQTTBrokerKw.class);
        this.keywordSet.put("create_mqtt_client", CreateMQTTClientKw.class);
        this.keywordSet.put("create_rest_tester", CreateRESTTesterKw.class);
        this.keywordSet.put("create_soap_tester", CreateSOAPTesterKw.class);
        this.keywordSet.put("create_telnet_client", CreateTelnetClientKw.class);
        this.keywordSet.put("create_telnet_server", CreateTelnetServerKw.class);
        this.keywordSet.put("create_web_crawler", CreateWebCrawlerKw.class);
        // keyword list /////////////////////////////////////////////////////
    }

    public static NATTContext instance() {
        if (cntx == null) {
            NATTContext.cntx = new NATTContext();
        }
        return NATTContext.cntx;
    }

}
