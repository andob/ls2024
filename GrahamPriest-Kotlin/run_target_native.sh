#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-native'" >> settings.gradle

./gradlew :target-native:clean :target-native:runDebugExecutableNative