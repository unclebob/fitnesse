#!/bin/sh
# stop.sh - Start a running FitNesse server. You must specify the same port used with "run.sh".

java -cp fitnesse.jar fitnesse.Shutdown "$@"
