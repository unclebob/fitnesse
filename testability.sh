#!/bin/bash
# Run Testability Explorer V1.2 on FitNesse. Build fitnesse first.
# Download it from http://code.google.com/p/testability-explorer/
# Define $TESTABILITY_EXPLORER_HOME to point to the installation location.
# Options:
#  -print (html|detail)
# See also options documented in the TE readme.

classpath=classes:dist/fitnesse/fitlibrary.jar:lib/easymock.jar:lib/junit.jar
# for jar in lib/*.jar
# do
# 	test -s $jar &&	classpath=$classpath:$jar
# done
echo "Invoking: java -jar $TESTABILITY_EXPLORER_HOME/testability-explorer-1.2.0-r54.jar -cp $classpath fitnesse $@" 1>&2
java -jar $TESTABILITY_EXPLORER_HOME/testability-explorer-1.2.0-r54.jar -cp $classpath fitnesse "$@"
