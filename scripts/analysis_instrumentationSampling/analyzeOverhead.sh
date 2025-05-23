#!/bin/bash

function getSum {
  awk -vOFMT=%.10g '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}

function getOverhead {
	echo "#CallTreeDepth Deviation Mean"
	for size in 2 4 6 8 10 12 14 16
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

folders=("overhead-pure" "sampling-interval1" "sampling-interval10" "sampling-interval20" "instrumentation-usc" "instrumentation-complete")

start=$(pwd)

for folder in "${folders[@]}"
do
	if [ ! -d $1/$folder ]
	then
		echo "Folder $1/$folder should be present; skipping"
	else
		cd $1/$folder
		getOverhead &> $1/$folder.csv
	fi
done

cd $1
gnuplot -c $start/plotOverhead.plt
