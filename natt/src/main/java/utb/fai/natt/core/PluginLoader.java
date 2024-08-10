package utb.fai.natt.core;

import utb.fai.natt.spi.INATTPlugin;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Trida pro nacteni a inicializaci pluginu.
 */
public class PluginLoader {

    public static String pluginsDirectory = "plugins";

    protected NATTLogger logger = new NATTLogger(PluginLoader.class);
    protected NATTContext context;

    protected List<INATTPlugin> plugins;

    public PluginLoader(NATTContext context) {
        this.context = context;
        this.plugins = new LinkedList<INATTPlugin>();
    }

    /**
     * Nacte a inicializuje pluginy z daneho adresare.
     */
    public void loadPlugins() {
        File dir = new File(pluginsDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warning("Plugins directory not found.");
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            logger.info("No plugins found.");
            return;
        }

        for (File file : files) {
            try {
                loadPlugin(file);
            } catch (Exception e) {
                logger.error("Failed to load plugin [" + file.getName() + "]: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Nacte plugin z danaho souboru.
     * 
     * @param file Soubor s pluginy
     * @throws Exception
     */
    public void loadPlugin(File file) throws Exception {
        URL[] urls = { file.toURI().toURL() };
        try (URLClassLoader classLoader = new URLClassLoader(urls, PluginLoader.class.getClassLoader())) {

            // nacte manifest
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(new File(file.getParent(), "plugin.properties"))) {
                properties.load(fis);
            }

            // nacte hlavni tridu s pluginem
            String mainClassName = properties.getProperty("Main-Class");
            if (mainClassName == null) {
                throw new IOException("No Main-Class defined in plugin.properties for " + file.getName());
            }

            // kontrola, zda je trida implementuje INATTPlugin
            Class<?> pluginClass = classLoader.loadClass(mainClassName);
            if (!INATTPlugin.class.isAssignableFrom(pluginClass)) {
                throw new ClassCastException("Class " + mainClassName + " does not implement INATTPlugin");
            }

            // inicializace a registrace pluginu
            INATTPlugin plugin = (INATTPlugin) pluginClass.getDeclaredConstructor().newInstance();
            if (plugin != null) {
                plugin.initialize(this.context);
                this.plugins.add(plugin);
                logger.info("Successfully loaded and initialized plugin ["
                        + plugin.getName() + "] from file: " + file.getName());
            } else {
                logger.error("Failed to initialize plugin from file: " + file.getName());
            }
        }
    }

    /**
     * Vrati seznam nactenych pluginu.
     * 
     * @return Seznam nactenych pluginu
     */
    public List<INATTPlugin> getPlugins() {
        return plugins;
    }

}