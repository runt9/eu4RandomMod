#!/bin/bash -e

rm -fv randomizer.jar
rm -fv ./common/ideas/00_country_ideas.txt
rm -fv ./common/prices/00_prices.txt
find ./history/provinces/ -type f -exec rm -v {} \;
find ./history/countries/ -type f -exec rm -v {} \;
cp -v ../randomizer/build/libs/randomizer-0.0.1.jar randomizer.jar
java -jar randomizer.jar
zip -r runt9-randomizer.zip common/ history/ descriptor.mod
mv -v runt9-randomizer.zip /mnt/hgfs/vmshare/
