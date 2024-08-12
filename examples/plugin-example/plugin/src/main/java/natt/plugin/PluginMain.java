package natt.plugin;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.INATTPlugin;

public class PluginMain implements INATTPlugin {

    public static final String NAME = "My Plugin";

    @Override
    public String getName() {
        // name of your plugin
        return PluginMain.NAME;
    }

    @Override
    public void initialize(INATTContext ctx) {
        // Register all your keywords here
        ctx.registerKeyword("my_keyword_1", MyKeyword1.class);     
        // Register all your modules here
        ctx.registerModule("my_module_1", MyModule1.class);
    }
    
}
