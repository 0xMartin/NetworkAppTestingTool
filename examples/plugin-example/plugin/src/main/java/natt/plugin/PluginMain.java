package natt.plugin;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.INATTPlugin;

public class PluginMain implements INATTPlugin {

    @Override
    public String getName() {
        return "My Plugin";
    }

    @Override
    public void initialize(INATTContext ctx) {
        // Register your keywords here
        ctx.registerKeyword(new MyKeyword1());     
    }
    
}
