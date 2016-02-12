# Build RangeFix

JDK 7 and Scala 2.9 are required to build RangeFix.

## How to set up on Windows

*(Tested on Windows 7 32-bit)*

Download and install:

* Java JDK 7u79: <http://download.oracle.com/otn-pub/java/jdk/7u79-b15/jdk-7u79-windows-i586.exe>
* Scala 2.9.2: <http://scala-lang.org/files/archive/scala-2.9.2.msi>
* sbt 0.13.9: <https://dl.bintray.com/sbt/native-packages/sbt/0.13.9/sbt-0.13.9.msi>

Append `;C:\Program Files\Java\jdk1.7.0_79\bin` to the end of the environment
varible `Path`.

Create environment variable `JAVA_TOOL_OPTIONS` with value
`-Dfile.encoding=UTF8`.

## How to set up on Linux

*(Tested on Fedora 23 64-bit)*

Download and extract:

* OpenJDK 1.7.0 u80: <https://bitbucket.org/alexkasko/openjdk-unofficial-builds/downloads/openjdk-1.7.0-u80-unofficial-linux-amd64-image.zip>
* Scala 2.9.2: <http://scala-lang.org/files/archive/scala-2.9.2.tgz>

Install sbt and Z3: `sudo dnf install sbt z3`

Run:

    $ PATH=`readlink -f openjdk-1.7.0-u80-unofficial-linux-amd64-image/bin`:$PATH
    $ PATH=`readlink -f scala-2.9.2/bin`:$PATH
    $ cd rangeFix
    $ git apply linux-z3-path.patch
    $ sbt test

## How to build RangeFix.jar

    $ ./build-combined-jar.sh

# Run RangeFix

Running RangeFix.jar works with both JRE 7 and JRE 8.

    $ java -cp RangeFix.jar ca.uwaterloo.gsd.rangeFix.KconfigMain testfiles/kconfig/test.exconfig testfiles/kconfig/test.config A yes D yes

