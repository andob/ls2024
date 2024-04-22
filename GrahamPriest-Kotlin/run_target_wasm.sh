#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-wasm'" >> settings.gradle

./gradlew :target-wasm:clean :target-wasm:wasmBrowserRun