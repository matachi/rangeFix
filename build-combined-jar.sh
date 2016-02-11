#!/usr/bin/env bash

sbt package
test -d tmp && rm -r tmp
mkdir tmp
pushd tmp
jar -xf ../target/scala-2.9.2/root_2.9.2-0.1-SNAPSHOT.jar
jar -xf ../lib/kiama_2.9.2-1.4.0.jar
jar -xf ../lib/scala-library.jar
jar -xf ../lib/lvat-0.5-SNAPSHOT.jar
popd
jar -cvf RangeFix.jar -C tmp .
