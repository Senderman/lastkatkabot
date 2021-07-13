#!/usr/bin/env bash

projectdir=`pwd`
builddir=$projectdir/build
workdir=$builddir/target/native


jar() {
    cd $projectdir
    echo "Building Spring Boot Jar..."
    ./gradlew bootJar
}

clean() {
    cd $projectdir
    rm -rf $workdir
    mkdir -p $workdir
    cd $workdir
}

unpack() {
    cd $workdir
    echo "unpacking jar to $workdir"
    echo "============================ $builddir"
    unzip  $builddir/libs/lastkatkabot-1.0.jar 
    ls $builddir/libs
    cp -R META-INF BOOT-INF/classes
}

trace() {
    cd $projectdir
    echo "Running jar with native-image-agent to collect runtime information for native-image"
    echo "Hit Ctrl-C to finish"
    spring_profiles_active=test \
            java \
            -DspringAot=true \
            -agentlib:native-image-agent=access-filter-file=access-filter.json,config-merge-dir=$workdir/BOOT-INF/classes/META-INF/native-image/org.springframework.aot/spring-aot \
            -jar $builddir/libs/lastkatkabot-1.0.jar

}

native() {
    echo "\nBuilding native image to $builddir/target"
    cd $workdir
    native-image \
            --initialize-at-build-time=sun.instrument.InstrumentationImpl \
            --enable-https -H:Name=lastkatkabot \
            -cp BOOT-INF/classes:`find BOOT-INF/lib | tr '\n' ':'`
    mv lastkatkabot ..
}

clean
jar
unpack
trace
native
