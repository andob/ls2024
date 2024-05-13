#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-wasm'" >> settings.gradle

rm -rf ./build
rm ./kotlin-js-store/yarn.lock

./gradlew :target-wasm:clean :target-wasm:wasmBrowserRun