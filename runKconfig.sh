#!/usr/bin/env bash

java -cp "/home/matachi/dev/vm/rangeFix/target/scala-2.9.2/classes:/home/matachi/dev/vm/rangeFix/lib/kiama_2.9.2-1.4.0.jar:/home/matachi/dev/vm/rangeFix/lib/scala-library.jar:/home/matachi/dev/vm/rangeFix/lib/lvat-0.5-SNAPSHOT.jar" ca.uwaterloo.gsd.rangeFix.KconfigMain $1 $2 $3 $4
