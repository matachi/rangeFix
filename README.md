Steps to build the project:

    $ cd ~
    $ git clone git@github.com:matachi/linux-variability-analysis-tools.git
    $ git submodule init
    $ git submodule update
    $ cd linux-variability-analysis-tools
    $ sbt
    > compile
    > package
    > project fm-translation
    > compile
    > package
    > exit

    $ cd ~
    $ git clone git@github.com:matachi/rangeFix.git
    $ cd rangeFix
    $ mkdir lib
    $ cp ../linux-variability-analysis-tools/target/scala-2.11/lvat_2.11-1.0-SNAPSHOT.jar lib
    $ cp ../linux-variability-analysis-tools/fm-translation/target/scala-2.11/fm-translation_2.11-0.5-SNAPSHOT.jar lib
    $ sbt
    > compile

