#!/bin/sh
# run.sh - Start FitNesse.
# try "run.sh -h" for help.

function help
{
	echo "Usage: run.sh [-p port] [-d dir] [-r root] [-l logDir] [-e days] [-o] [-a userpass] [-J jvm_option ...]
	-p <port number>                   Liston on the specified port (default: 80)
	-d <working directory>             Working directory (default: ".")
	-r <page root directory>           "Root" (default: FitNesseRoot)
	-l <log directory>                 Log to the specified directory (no logging is done, if omitted).
	-e <days>                          Number of days before page versions expire (default: 14).
	-o omit updates                    Don't do updates from remote wikis, if any.
	-a {user:pwd | user-file-name}     Enable authentication.
	-J <jvm option>                    Arguments to pass to the JVM (can be repeated).
	-h                                 This help message. Exits after printing..."
}

declare -a jvm_args
declare -a fitnesse_args
while [ $# -gt 0 ]
do
	case $1 in
		-h|-help)
			help "$@"
			exit 0
			;;
		-J)
			shift
			jvm_args[${#jvm_args[*]}]=$1
			;;
		-[pdrleoa]*)
			fitnesse_args[${#fitnesse_args[*]}]=$1
			shift
			fitnesse_args[${#fitnesse_args[*]}]=$1
			;;
		*)
			echo "Unknown argument specified: $1"
			help "$@"
			exit 1
			;;
	esac
	shift
done
jvm_args[${#jvm_args[*]}]="-jar"
jvm_args[${#jvm_args[*]}]="fitnesse.jar"

echo java ${jvm_args[*]} ${fitnesse_args[*]}
java -Xmx100M ${jvm_args[*]} ${fitnesse_args[*]}


