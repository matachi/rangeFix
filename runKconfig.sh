#!/usr/bin/env bash

java -cp "target/scala-2.9.2/classes:lib/kiama_2.9.2-1.4.0.jar:lib/scala-library.jar:lib/lvat-0.5-SNAPSHOT.jar" ca.uwaterloo.gsd.rangeFix.KconfigMain $1 $2 $3 $4
