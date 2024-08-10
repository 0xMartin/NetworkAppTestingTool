package utb.fai.natt.spi;

/**
 * Interface for NATT plugins.
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
