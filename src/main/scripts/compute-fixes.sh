#!/bin/sh
#
# Linux Shell Script to recover different versions of the same file from a git repository
#
# Written by Arnaud Hubaux <ahubaux@gmail.com>
#
# USAGE
# sh compute-fixes <source-folder> <config_folder>

# VARIABBLES

# MAIN

rm -rf $2*.conflict
rm -rf $2*.fix
for f in $2*.ecc
do
	echo "Processing file - $f"
	./ecosconfig.exe --srcdir=$1 --config=$f resolve > $f.fix
done