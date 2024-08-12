#!/usr/bin/env bash

./gradlew :natt-spi:clean

./gradlew :natt-spi:build

cp natt-spi/build/libs/natt-spi-*.jar natt/libs && echo "Copied natt-spi to natt/libs"

cp natt-spi/build/libs/natt-spi-*.jar examples/plugin-example/plugin/libs && echo "Copied natt-spi to examples/plugin-example/plugin/libs"