package utb.fai.natt.core;

import utb.fai.natt.spi.INATTPlugin;
import utb.fai.natt.spi.INATTContext;

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

    /**
     * Nacte a inicializuje pluginy z daneho adresare.
     */
    public void loadPlugins() {
        File dir = new File(pluginsDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Plugins directory not found.");
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            System.out.println("No plugins found.");
            return;
        }

        for (File file : files) {
            try {
                loadPlugin(file);
            } catch (Exception e) {
                System.err.println("Failed to load plugin: " + file.getName());
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
    private void loadPlugin(File file) throws Exception {
        URL[] urls = { file.toURI().toURL() };
        try (URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader())) {

            // Načti manifest
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(new File(file.getParent(), "plugin.properties"))) {
                properties.load(fis);
            }

            String mainClassName = properties.getProperty("Main-Class");
            if (mainClassName == null) {
                throw new IOException("No Main-Class defined in plugin.properties for " + file.getName());
            }

            Class<?> pluginClass = classLoader.loadClass(mainClassName);
            if (!INATTPlugin.class.isAssignableFrom(pluginClass)) {
                throw new ClassCastException("Class " + mainClassName + " does not implement INATTPlugin");
            }

            INATTPlugin plugin = (INATTPlugin) pluginClass.getDeclaredConstructor().newInstance();
            if(plugin != null) {
                plugin.initialize(NATTContext.instance());
            } else {

            }

            System.out.println("Successfully loaded and initialized plugin: " + mainClassName);
        }
    }

    /**
     * Nastavi cestu k adresari s pluginy
     * 
     * @param pluginsDirectory Cesta k adresari s pluginy
     */
    public static void setPluginsDirectory(String pluginsDirectory) {
        PluginLoader.pluginsDirectory = pluginsDirectory;
    }

    /**
     * Vraci cestu k adresari s pluginy
     * 
     * @return Cesta k adresari s pluginy
     */
    public static String getPluginsDirectory() {
        return pluginsDirectory;
    }

}