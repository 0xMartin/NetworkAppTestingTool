#!/usr/bin/env bash

./gradlew build -x test

cp app/build/libs/app-all.jar natt-config-editor/NATT.jar && echo "NATT.jar moved to config-editor"

