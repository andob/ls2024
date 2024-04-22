#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-jvm'" >> settings.gradle

./gradlew :target-jvm:clean :target-jvm:jar

find ./target-jvm/build/libs -name "*.jar" -exec java -jar {} \;