#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-js'" >> settings.gradle

./gradlew :target-js:clean :target-js:jsBrowserRun