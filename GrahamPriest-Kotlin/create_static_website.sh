#!/bin/bash
./create_backend_artifact.sh

cd ./backend
php index.php build_static_website index > index.html

while IFS= read -r line
do
  if [[ "$line" == "["* ]] || [[ "$line" == *"]" ]]
  then
    problem_name=`echo "${line:1:${#line}-2}"`
    php index.php build_static_website $problem_name > "$problem_name.html"
  fi
done < './demo.ini'

zip -r artifact.zip *.html
zip -d artifact.zip index.php
rm *.html
cd ..
