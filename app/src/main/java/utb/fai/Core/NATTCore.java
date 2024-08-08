package utb.fai.Core;

import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.NonUniqueTestNamesException;
import utb.fai.IO.LocalHostIO;
import utb.fai.IO.NetworkIO;
import utb.fai.Module.ExternalProgramRunner;
import utb.fai.ReportGenerator.TestReportGenerator;

import org.apache.commons.cli.*;

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

    // verze nastroje
    public static final String VERSION = "1.5.0";

    // defaultni cesta k vystupnimu souboru z reportem o testovani
    public static final String REPORT_PATH = "test_report.html";

    // hlavni moduly
    protected ExternalProgramRunner programRunner;
    protected LocalHostIO localHostIO;
    protected NetworkIO networkIO;
    protected NATTLogger logger = new NATTLogger(NATTCore.class);

    // root keyworda, kterou se zacne vykonavani celeho testovani
    protected Keyword rootKeyword;

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
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        // definice parametru
        options.addOption("c", "config", true, "Path to the testing configuration file (loaded from the local host)");
        options.addOption("nc", "net-config", true,
                "Path to the testing configuration file (loaded from a URL address)");
        options.addOption("t", "title", true, "Title/header of the resulting testing report");
        options.addOption("h", "help", false, "Help on how to use");
        options.addOption("v", "validate", false, "Validates the test suite configuration");

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new InternalErrorException("Failed to parse command line arguments.");
        }

        // help
        boolean helpRequested = cmd.hasOption("h");
        if (helpRequested) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar NATT.jar", options);
            System.exit(0);
        }

        // title
        this.testReportName = cmd.getOptionValue("t");

        // c parametr
        String buffer = cmd.getOptionValue("c");
        if (buffer != null) {
            this.configPath = buffer;
            this.loadConfigFromLocalHost = true;
        }

        // nc parametr
        buffer = cmd.getOptionValue("nc");
        if (buffer != null) {
            this.configPath = buffer;
            this.loadConfigFromLocalHost = false;
        }

        // validate only parameter
        if (cmd.hasOption("v")) {
            this.validateOnly = true;
        } else {
            this.validateOnly = false;
        }

        // overeni zda je nastavena cesta ke konfiguraci
        if (configPath == null) {
            throw new InternalErrorException("It is necessary to specify the path to the configuration file!");
        }

        // vytvoreni instacni trid pro nacitani
        this.localHostIO = new LocalHostIO(configPath);
        this.networkIO = new NetworkIO(configPath);

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

    public String getConfigPath() {
        return configPath;
    }

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
        Keyword root = NATTTestBuilder.buildTests(this.configurationData);

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
        this.rootKeyword.execute();

        // volani ukoncujicich akci
        this.rootKeyword.deleteAction();

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
