#!/usr/bin/env bash
./gradlew bootJar
workdir=build/target/native
rm -rf $workdir
mkdir -p $workdir
cd $workdir
jar -xvf ../../libs/lastkatkabot-1.0.jar >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes
native-image -H:Name=lastkatkabot -cp BOOT-INF/classes:`find BOOT-INF/lib | tr '\n' ':'`
mv lastkatkabot ..
