#!/usr/bin/env bash
./gradlew bootJar
workdir=build/target/native
#rm -rf $workdir
mkdir -p $workdir
cd $workdir
jar -xvf ../../libs/lastkatkabot-1.0.jar >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes
cd ../../../

spring_profiles_active=test java -DspringAot=true -agentlib:native-image-agent=access-filter-file=access-filter.json,config-merge-dir=$workdir/BOOT-INF/classes/META-INF/native-image/org.springframework.aot/spring-aot -jar build/libs/lastkatkabot-1.0.jar

native-image --initialize-at-build-time=sun.instrument.InstrumentationImpl --enable-https -H:Name=lastkatkabot -cp BOOT-INF/classes:`find BOOT-INF/lib | tr '\n' ':'

mv lastkatkabot ..
