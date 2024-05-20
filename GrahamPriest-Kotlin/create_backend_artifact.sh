#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-js'" >> settings.gradle

rm -rf ./build
rm -rf ./target-js/build
rm -rf ./kotlin-js-store

./gradlew :target-js:clean :target-js:jsBrowserWebpack

cp ./target-js/build/dist/js/productionExecutable/target-js.js ./backend
cp ./target-js/build/dist/js/productionExecutable/target-js.js.map ./backend

zip ./backend/artifact.zip \
  ./backend/demo.ini \
  ./backend/index.php \
  ./backend/target-js.js \
  ./backend/target-js.js.map \
  ./backend/keyboard.js \
  ./backend/tree-prettifier.js
