# How to set up on Windows

*(Tested on Windows 7 32-bit)*

Download and install:

* Java JDK 7u79: <http://download.oracle.com/otn-pub/java/jdk/7u79-b15/jdk-7u79-windows-i586.exe>
* Scala 2.9.2: <http://scala-lang.org/files/archive/scala-2.9.2.msi>
* sbt 0.13.9: <https://dl.bintray.com/sbt/native-packages/sbt/0.13.9/sbt-0.13.9.msi>

Append `;C:\Program Files\Java\jdk1.7.0_79\bin` to the end of the environment
varible `Path`.

Create environment variable `JAVA_TOOL_OPTIONS` with value
`-Dfile.encoding=UTF8`.

