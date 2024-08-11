package utb.fai.natt.core;

import utb.fai.natt.NetworkAppTestingTool;
import utb.fai.natt.spi.INATTPlugin;
import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.KeywordDocumentation;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.StatusCode;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;
import utb.fai.natt.spi.exception.NonUniqueTestNamesException;
import utb.fai.natt.spi.NATTLogger;

import utb.fai.natt.io.LocalHostIO;
import utb.fai.natt.io.NetworkIO;
import utb.fai.natt.reportGenerator.TestReportGenerator;

import org.apache.commons.cli.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

/**
 * Hlavni trida testovaciho nastroju. Zajistuje pospupne zpracovani vsech testu
 * a vykonani vsech pozadovanych kroku. To zahrnuje nacteni konfigurace ze
 * souboru, jeho prevod na pozadovanou datovou strukturu urcenou pro vykonavani,
 * nasledne jeji vykonani a nakonec take vygenerovani reportu testovani.
 */
public class NATTCore {

    // verze nastroje (nacte z package.json)
    public static final String VERSION = NetworkAppTestingTool.class.getPackage().getImplementationVersion();

    // defaultni cesta k vystupnimu souboru z reportem o testovani
    public static final String REPORT_PATH = "test_report.html";

    // hlavni moduly
    protected NATTLogger logger = new NATTLogger(NATTCore.class);
    protected PluginLoader pluginLoader;
    protected LocalHostIO localHostIO;
    protected NetworkIO networkIO;

    // root keyworda, kterou se zacne vykonavani celeho testovani
    protected NATTKeyword rootKeyword;

    // pracovni promenne
    protected Map<String, Object> configurationData;

    // nactene argumenty
    private String configPath;
    private String testReportName;
    private boolean loadConfigFromLocalHost;
    private boolean validateOnly;

    /**
     * Inicializace jadra black box testovaciho nastroje
     * 
     * @param args Mezi vyzadovane argumenty patri -a --application
     *             <path_to_tested_app>. Pomoci tohoto argumentu je nastavena cesta
     *             k aplikaci, ktera bude testovana. Dalsi argument je -c --config
     *             <path_fo_config> pomoci ktere ho je nastavena cesta ke
     *             konfiguracnimu souboru z testy. Je mozne pouzit i jeho variantu
     *             -nc --net-config <url_of_config> ktery funguje uplne stejne, ale
     *             soubora necita z localhostu, ale vybrane url adresy. Musi byt
     *             specifikovani bude -n nebo -nc. Parametrem -t --title je mozne
     *             nastavit nazev/titulek vystupniho reportu (tento parametr je
     *             volitelny)
     * @throws InternalErrorException
     */
    public NATTCore(String[] args) throws InternalErrorException {

        // inicializace modulu
        this.localHostIO = new LocalHostIO("test-config.yaml");
        this.networkIO = new NetworkIO("");
        this.pluginLoader = new PluginLoader(NATTContext.instance());
        this.pluginLoader.loadPlugins();

        /************************************************************************************************************************* */

        // zpracovani argumentu
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        // definice parametru
        options.addOption("c", "config", true, "Path to the testing configuration file (loaded from the local host)");
        options.addOption("nc", "net-config", true,
                "Path to the testing configuration file (loaded from a URL address)");
        options.addOption("t", "title", true, "Title/header of the resulting testing report");
        options.addOption("h", "help", false, "Help on how to use");
        options.addOption("v", "validate", false, "Validates the test suite configuration");
        options.addOption("p", "plugins", false, "List of all loaded plugins");
        options.addOption("kd", "keywordDoc", false,
                "List of all registered keywords and their documentation in json format");
        options.addOption("k", "keywords", false, "List of all registered keywords");

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new InternalErrorException("Failed to parse command line arguments.");
        }

        /************************************************************************************************************************* */

        // title vystupniho reportu
        this.testReportName = cmd.getOptionValue("t");

        // c parametr
        String buffer = cmd.getOptionValue("c");
        if (buffer != null) {
            this.configPath = buffer;
            this.localHostIO.setFileName(this.configPath);
            this.loadConfigFromLocalHost = true;
        }

        // nc parametr
        buffer = cmd.getOptionValue("nc");
        if (buffer != null) {
            this.configPath = buffer;
            this.networkIO.setUrl(this.configPath);
            this.loadConfigFromLocalHost = false;
        }

        // validace konfiguracniho souboru
        if (cmd.hasOption("v")) {
            this.validateOnly = true;
        } else {
            this.validateOnly = false;
        }

        // napoveda k pouziti
        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar NATT.jar", options);
            System.exit(0);
        }

        // zobrazi vsechny nactene pluginy
        if (cmd.hasOption("p")) {
            int i = 1;
            System.out.println("Loaded plugins:");
            for (INATTPlugin plugin : this.pluginLoader.getPlugins()) {
                System.out.println(String.format("%d: %s", i++, plugin.getName()));
            }
            System.exit(0);
        }

        // zobrazi seznam vsech registrovanych keywordu (jejich nazvu)
        if (cmd.hasOption("k")) {
            for (Map.Entry<String, java.lang.Class<?>> entry : NATTContext.instance().getKeywordSet().entrySet()) {
                try {
                    java.lang.Class<?> keywordClass = entry.getValue();
                    java.lang.reflect.Constructor<?> constructor = keywordClass.getDeclaredConstructor();
                    NATTKeyword keywordInstance = (NATTKeyword) constructor.newInstance();
                    if (keywordInstance != null) {
                        System.out.println(keywordInstance.getKeywordName());
                    }
                } catch (Exception e) {
                    System.out.println("Failed to create keyword instance for keyword " + entry.getKey() + ".");
                }
            }
            System.exit(0);
        }

        // ve json formatu vypisi dokumentaci vsech registrovanych keyword
        if (cmd.hasOption("kd")) {
            LinkedList<KeywordDocumentation> documentationList = new LinkedList<KeywordDocumentation>();

            for (Map.Entry<String, java.lang.Class<?>> entry : NATTContext.instance().getKeywordSet().entrySet()) {
                try {
                    java.lang.Class<?> keywordClass = entry.getValue();
                    java.lang.reflect.Constructor<?> constructor = keywordClass.getDeclaredConstructor();
                    NATTKeyword keywordInstance = (NATTKeyword) constructor.newInstance();
                    KeywordDocumentation doc = keywordInstance.getDocumentation();
                    if (doc != null) {
                        documentationList.add(doc);
                    }
                } catch (Exception e) {
                    logger.warning("Failed to create keyword instance for keyword " + entry.getKey() + ".");
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(documentationList);
                System.out.println("Documentation for registered keywords:");
                System.out.println(json);
            } catch (JsonProcessingException e) {
                logger.error("Failed to convert keyword documentation to JSON.");
            }

            System.exit(0);
        }

        /************************************************************************************************************************* */

        // overeni zda je nastavena cesta ke konfiguraci
        if (configPath == null) {
            throw new InternalErrorException("It is necessary to specify the path to the configuration file!");
        }

        // ukonceni predchozich procesu, ktere se nepodarilo spravne ukoncit
        try {
            ProcessManager.killProcessesAndCleanUp(ProcessManager.DEFAULT_FILE);
        } catch (Exception ex) {
        }

        // info
        logger.info(String.format(
                "NATT CORE initialization done\nVersion: %s \nConfiguration path: %s\nConfiguration loading mode: %s",
                NATTCore.VERSION, configPath, loadConfigFromLocalHost ? "FROM HOST" : "FROM URL"));
        String currentDirectory = System.getProperty("user.dir");
        logger.info("Working directory path: " + currentDirectory);
    }

    /**
     * Vrati cestu ke konfiguraci
     * 
     * @return Cesta ke konfiguraci
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * Nastavi cestu ke konfiguraci
     * 
     * @param configPath cesta ke konfiguraci
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /**
     * Ukonci vsechny aktualne spustene moduly
     */
    public static void termiteAllModules() {
        LinkedList<NATTModule> modules = NATTContext.instance().getModules();
        modules.stream().forEach((m) -> {
            m.terminateModule();
        });
        NATTContext.instance().getModules().clear();
    }

    /**
     * Nacte obsah konfiguracniho souboru, ktery bude obsahovat popis testovani.
     * 
     * @throws InvalidSyntaxInConfigurationException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void loadConfigFile() throws InvalidSyntaxInConfigurationException, IOException {
        logger.info("Start loading of the configuration file ...");

        if (loadConfigFromLocalHost) {
            Object configObj = localHostIO.loadFromYaml();
            if (configObj instanceof Map) {
                this.configurationData = (Map<String, Object>) configObj;
            } else {
                throw new InvalidSyntaxInConfigurationException("Missing test_root element.");
            }
        } else {
            Object configObj = networkIO.loadFromYaml();
            if (configObj instanceof Map) {
                this.configurationData = (Map<String, Object>) configObj;
            } else {
                throw new InvalidSyntaxInConfigurationException("Missing test_root element.");
            }
        }

        logger.info("Configuration loading done");
    }

    /**
     * Podle nactene konfigurace sestavi testovaci sadu. Bude sestavena jeji
     * struktura a budou take nacteny vsechny parametry.
     * 
     * @throws InvalidSyntaxInConfigurationException
     * @throws InternalErrorException
     */
    public void buildTestsFromYaml()
            throws InvalidSyntaxInConfigurationException, InternalErrorException {
        logger.info("Start building test structure according to the configuration ...");

        if (this.configurationData == null) {
            throw new InternalErrorException("Configuration not loaded!");
        }

        // Sestaveni testovaci sady dle konfigurace probehne v externim modulu. Dojde k
        // vytvoreni struktury trid a naplneni data z konfiguracniho souboru
        NATTKeyword root = NATTTestBuilder.buildTests(this.configurationData);

        if (root != null) {
            this.rootKeyword = root;
        } else {
            throw new InternalErrorException("Failed to generate tests. Root keyword is null!");
        }

        // vypis testovaci stuktury na system.out
        this.logger.info("Test structure ...");
        root.printToStructure(0);

        logger.info("Test structure building done");
    }

    /**
     * Vykona vsechny keywordy ve vygenerovane testovaci sade. Jake prvni se vsechny
     * keywordy v testovaci sade inicializuji a nasledne se zacnou vykonavat v
     * definovanem poradi. Nakonec se vykonaji ukoncujici akce.
     * 
     * @throws InternalErrorException
     * @throws InvalidSyntaxInConfigurationException
     * @throws NonUniqueModuleNamesException
     */
    public void executeAllTests()
            throws InternalErrorException, InvalidSyntaxInConfigurationException,
            NonUniqueModuleNamesException, NonUniqueTestNamesException {
        logger.info("Start test executing ...");

        if (this.rootKeyword == null) {
            throw new InternalErrorException("The test_root element is not initialized!");
        }

        // vygenerovani extentu pro generovani reportu testovani
        NATTContext.instance().setReportExtent(TestReportGenerator.generateReportExtent(NATTCore.REPORT_PATH,
                this.testReportName == null ? "Test report" : this.testReportName));

        // clear test caseses results
        NATTContext.instance().getTestCaseResults().clear();

        // inicializace vsech keyword v sestavene testovaci sade
        NATTTestBuilder.initTestScructure(this.rootKeyword);

        // pokud je vyzadovana jen validace konfiguracniho souboru tak zde ukonci
        // testovaci nastroj. Pokud vse az do tohoto kroku probehlo v poradku bez
        // vyvolani vyjimky tak je konfigurace testovaci sady platna z hlediska
        // syntakticke stranky.
        if (this.validateOnly) {
            System.exit(0);
        }

        // zahaji testovani (testovani je zahajeno vykonavanim hlavni keywordy a pak se
        // pokracuje na jejich potomcich)
        logger.info("Starts execution on the root keyword  ...");
        this.rootKeyword.execute(NATTContext.instance());

        // volani ukoncujicich akci
        this.rootKeyword.deleteAction(NATTContext.instance());

        logger.info("Test executing finished");
    }

    /**
     * Vygeneruje report o provedenych testech. Jako prvni vygeneruj a ulozi
     * vystupni soubory obsahujici podrobny popis vysledku a prubehu testovani.
     * Nakonec tato metoda ukonci aplikaci s pozadovanym status kodem. Pokud vsechny
     * testy uspesne problehly bude navracen status kod 0.
     * 
     * @throws InternalErrorException
     */
    public void generateReport() throws InternalErrorException {
        logger.info("Start generating test report");

        // vygeneruje report
        try {
            TestReportGenerator.exportReportToFile(NATTContext.instance().getReportExtent());
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalErrorException("Failed to generate report!");
        }

        // urci finalni status testovani a podle jeho vysledku navrati prislusny status
        // kod
        boolean passed = NATTContext.instance().getTestCaseResults().stream().allMatch(tc -> tc.isPassed());
        if (NATTContext.instance().getMaxScore() > 0) {
            logger.info(String.format("Final score: %f", NATTContext.instance().getFinalScore()));
        }
        logger.info(String.format("Report generating done. Leaving status: %s", passed ? "PASSED" : "FAILED"));

        // navrati finalni status kod
        System.exit(passed ? StatusCode.TEST_PASSED : StatusCode.TEST_FAILED);
    }

}
