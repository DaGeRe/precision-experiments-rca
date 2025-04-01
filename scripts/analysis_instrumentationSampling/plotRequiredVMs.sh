#!/bin/bash


if [ $# -lt 1 ]
then
	echo "Arguments missing, requires folder"
	exit 1
fi

start=$(pwd)

cd $1

for depth in 2 4 6 8; do cat instrumentation.csv | grep "^$depth" &> instrumentation_$depth.csv; done
for depth in 2 4 6 8; do cat sampling.csv | grep "^$depth" &> sampling_$depth.csv; done

gnuplot -c $start/plotRequiredVMs.plt
