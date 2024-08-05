#!/usr/bin/env bash

./gradlew build -x test

cp app/build/libs/app-all.jar config-editor/NATT.jar && echo "NATT.jar moved to config-editor"

cp app/build/libs/app-all.jar vscode-extension/natt-configuration-editor/resources/NATT.jar && echo "NATT.jar moved to NATT vscode extension"
