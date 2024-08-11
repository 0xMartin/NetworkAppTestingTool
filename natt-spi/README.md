# NATT-SPI documentation

**[Go Back](../README.md)**

This repository contains the source code for the **NATT-SPI library**, which is utilized by the **NATT testing tool**. This library is essential for **implementing custom plugins within the NATT**.

The source code includes the necessary interfaces and classes to extend and customize the functionality of NATT, allowing developers **to create their own modules and integrate them seamlessly**.

Additionally, documentation for the SPI is available in the [./doc](./doc) directory, generated using Doxygen.

## How to add MATT-SPI dependency to project?

### 1 Download NATT-SPI

Download the required version of the **NATT-SPI** JAR file. Then, create a **lib** directory in your project and place the downloaded JAR file into it.

### 2 Add Local NATT Libraries

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

### 3 Include NATT-SPI Dependency

In the dependencies section, add a reference to the latest version of the NATT-SPI library:

```gradle
dependencies {
    // NATT SPI
    implementation name: 'natt-spi-1.0.0'
}
```

### 4 Set the Main Class

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
