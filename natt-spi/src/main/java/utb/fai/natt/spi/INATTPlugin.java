package utb.fai.natt.spi;

/**
 * Main interface for NATT plugins. Plugins are loaded by the NATT plugin loader
 * and after that are initialized. NATT Context is passed to the plugin.
 */
public interface INATTPlugin {

    /**
     * Plugin name.
     */
    String getName();

    /**
     * Initializes the NATT plugin.
     * 
     * @param ctx the NATT context
     */
    void initialize(INATTContext ctx);

}
