#!/usr/bin/bash

if [ $# -ne 2 ]; then
	echo "2 args needed -> 1. file id in google drive  2. file name to save"
	exit -1
fi

ID=$1
FNAME=$2

curl -c ./cookie -s -L "https://drive.google.com/uc?export=download&id=$ID" > /dev/null
curl -Lb ./cookie "https://drive.google.com/uc?export=download&confirm=`awk '/download/ {print $NF}' ./cookie`&id=$ID" -o $FNAME

rm cookie

