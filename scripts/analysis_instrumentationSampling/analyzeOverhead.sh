#!/bin/bash

function getSum {
  awk -vOFMT=%.10g '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}

function getOverhead {
	echo "#CallTreeDepth Deviation Mean"
	for size in 2 4 6 8 10
	do
		resultFile=project_"$size"_303_peass/measurementsFull/MainTest_testMe.json
		if [ -f $resultFile ]
		then
			echo -n "$size "
			cat project_"$size"_303_peass/measurementsFull/MainTest_testMe.json \
				| grep value | awk '{print $3}' | tr -d "," \
				| awk '{print $1/1000000}' | getSum
		fi
	done
}

if [ $# -lt 1 ]
then
	echo "Arguments missing, required folder"
	exit 1
fi

if [ ! -d $1/instrumentation ]
then
	echo "instrumentation folder needs to be present"
	exit 1
fi

if [ ! -d $1/sampling ]
then
	echo "sampling folder needs to be present"
	exit 1
fi

start=$(pwd)

cd $1/sampling
getOverhead &> $1/sampling.csv

cd $1/instrumentation
getOverhead &> $1/instrumentation.csv

cd $1
gnuplot -c $start/plotOverhead.plt
