# NATT Plugin Example

**[Go Back](../../README.md)**

This guide will walk you through the process of creating a custom plugin for the NATT black-box testing tool. Follow the steps below to set up your plugin, configure it, and define custom keywords.

**NATT-SPI documentation available [here](../../natt-spi)**

## 1. Initialize the Project

First, initialize your Java project using Gradle. Open your terminal and run:

```bash
gradle init
```

This command will set up a basic Gradle project structure.

## 2. Update build.gradle

After initializing the project, you need to modify the build.gradle file to include the necessary dependencies and configurations.

### 2.1 Add Local NATT Libraries

Locate the repositories block in your build.gradle file and add the following lines to include local NATT libraries:

```gradle
repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

    // Local NATT libs
    flatDir {
        dirs 'libs'
    }
}
```

### 2.2 Include NATT-SPI Dependency

In the dependencies section, add a reference to the latest version of the NATT-SPI library:

```gradle
dependencies {
    // NATT SPI
    implementation name: 'natt-spi-1.1.0'
}
```

### 2.3 Set the Main Class

Configure the jar task to specify the main class of your plugin:

```gradle
jar {
    manifest {
        attributes(
            'Main-Class': 'natt.plugin.PluginMain'
        )
    }
}
```

## 3. Add the NATT-SPI Library

Next, create a directory named `libs` within your project. Download the latest version of NATT-SPI and place it in the libs folder. Source code if NATT-SPI is in `natt-spi` folder in root directory of this repository.

**Download NATT-SPI from [here](https://github.com/0xMartin/NetworkAppTestingTool/releases)**

## 4. Define the Main Plugin Class

Now, it's time to create the **main class** for your plugin. This class must implements the **INATTPlugin** interface and define the plugin's name and initialization logic.

Create a new Java file in your project and add the following code:

```java
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
```

## 5. Define Custom Keywords

After defining the main plugin class, you can create custom keywords according to your needs. Below is an example of how to define a custom keyword:

```java
@NATTAnnotation.Keyword(
    name = "my_keyword_1",
    description = "This is my first keyword.",
    parameters = { "name" },
    types = { ParamValType.STRING },
    kwGroup = PluginMain.NAME
    )
public class MyKeyword1 extends NATTKeyword {

    protected String moduleName;

    private MyModule1 module;

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        // load keyword parameter value. name of parameter is "name" or
        // DEFAULT_PARAMETER_NAME (DEFAULT_PARAMETER_NAME = in yaml is no need to
        // specify parameter name, like this my_keyword_1: "module-1")
        ParameterValue val = this.getParameterValue(new String[] { NATTKeyword.DEFAULT_PARAMETER_NAME, "name" },
            ParamValType.STRING, true);
        if (val != null) {
            moduleName = val.getValue().toString();
        }
    }

    @Override
    public boolean execute(INATTContext ctx) throws InternalErrorException, NonUniqueModuleNamesException {
        // create instance of my module
        Class<?>[] types = { String.class, INATTContext.class };
        Object[] args = { this.moduleName, ctx };
        this.module = (MyModule1) ctx.createInstanceOfModule("my_module_1", types, args);
        if(this.module == null) {
            return false;
        }

        // run module
        this.module.runModule();

        return true;
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        // terminate module
        if (this.module != null) {
            this.module.terminateModule();
        }
    }

}
```

## 6. Define Custom Modules

The principle is the same as for the keyword definition. It is also necessary to register it in the NATT context. When creating module instance using the `ctx.createInstanceOfModule` method, the module remains active in the context until it is terminated.

```java
package natt.plugin;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

/**
 * This is your module. Modules are used to send and receive messages. Modules
 * are used by keywords. After creating an instance of a module, its reference
 * is automatically inserted into the NATTContext and you can access it from
 * several different keywords using this method "ctx.getModule(name)".
 */
@NATTAnnotation.Module("my_module_1")
public class MyModule1 extends NATTModule {

    private NATTLogger logger = new NATTLogger(MyModule1.class);

    public MyModule1(String name, INATTContext ctx) throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, ctx);
    }

    @Override
    public void runModule() throws InternalErrorException {
        logger.info(super.getNameForLogger() + " Is running now!");
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        logger.info(super.getNameForLogger() + " sending message: " + message);
        return true;
    }

    @Override
    public boolean terminateModule() {
        logger.info(super.getNameForLogger() + " is terminating...");
        this.getContext().removeActiveModule(super.getName());
        return true;
    }

}
```

## 7. Compile and Build

Once you've completed the steps above, compile your project using Gradle:

```bash
./gradlew build
```

This command will generate a JAR file containing your custom NATT plugin. You can now integrate it with the NATT testing tool. Move the generated JAR file to the `plugins` directory of the NATT tool.

**Structure:**
```
./plugins/your-plugin.jar
./NATT.jar
./test-config.yaml
```
