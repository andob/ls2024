#!/bin/bash
echo "rootProject.name = 'GrahamPriestDeduction'" > settings.gradle
echo "include ':target-js'" >> settings.gradle

rm -rf ./build
rm -rf ./target-js/build
rm -rf ./kotlin-js-store

./gradlew :target-js:clean :target-js:jsBrowserWebpack

cp ./target-js/build/dist/js/productionExecutable/target-js.js ./backend
cp ./target-js/build/dist/js/productionExecutable/target-js.js.map ./backend

cd ./backend
zip ./artifact.zip \
  ./demo.ini \
  ./chapters.json \
  ./index.php \
  ./target-js.js \
  ./target-js.js.map \
  ./keyboard.js \
  ./tree-prettifier.js \
  ./book.jpg \
  ./styles.css \
  ./yoyo-animation.js
cd ..
