#!/usr/bin/env bash

./gradlew :natt:build -x test

cp natt/build/libs/natt-*-all.jar natt-config-editor/NATT.jar && echo "NATT.jar moved to config-editor"
cp natt/build/libs/natt-*-all.jar examples/project-example/NATT.jar && echo "NATT.jar moved to project-example"

