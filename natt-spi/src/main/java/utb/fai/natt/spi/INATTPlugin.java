package utb.fai.natt.spi;

/**
 * Interface for NATT plugins.
 */
public interface INATTPlugin {
    
    /**
     * Initializes the NATT plugin.
     * 
     * @param ctx the NATT context
     */
    public void initialize(INATTContext ctx);

}
