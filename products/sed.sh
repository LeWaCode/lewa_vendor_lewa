#!/bin/bash
files=`find . -iname "cyanogen*.mk"`
for f in $files
do
echo $f
sed -e 's/PRODUCT_COPY_FILES\ \+\=\ \ \//g' $f > temp; mv temp $f
done
