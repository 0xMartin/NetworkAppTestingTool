#!/usr/bin/env bash

./gradlew :natt-spi:clean

./gradlew :natt-spi:build

cp natt-spi/build/libs/natt-spi-*.jar natt/libs && echo "Copied natt-spi to natt/libs"

cp natt-spi/build/libs/natt-spi-*.jar examples/plugin-example/plugin/libs && echo "Copied natt-spi to examples/plugin-example/plugin/libs"

./gradlew :natt:clean

./gradlew :natt:build

cp natt/build/libs/natt-*-all.jar natt-config-editor/NATT.jar && echo "NATT.jar moved to config-editor"
cp natt/build/libs/natt-*-all.jar examples/project-example/NATT.jar && echo "NATT.jar moved to project-example"