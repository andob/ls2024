#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-js'" >> settings.gradle

rm -rf ./build
rm ./kotlin-js-store/yarn.lock

./gradlew :target-js:clean :target-js:jsBrowserRun
