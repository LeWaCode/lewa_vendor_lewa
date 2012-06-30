#!/bin/bash
files=`find -iname "*.mk"`
for f in $files
do
echo $f
sed -e 's/cyanogen:/lewa\//g' $f > temp; mv temp $f
done
