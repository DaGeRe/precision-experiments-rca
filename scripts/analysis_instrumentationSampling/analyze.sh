#!/bin/bash

set -e

function getHeatmapData {
	index=$1
	outname=$2
	for file in $(ls | grep ".csv" | grep -v "_current_[0-9]*.csv" | grep -v "_predecessor_[0-9]*.csv")
	do
		cat $file \
		| awk '{print $2" "$3" "$'$index'}' \
		| sort -k 1 -k 2 -n \
		| awk -f $start/addblanks.awk \
		> $outname/$file
	done
}

start=$(pwd)

if [ $# -lt 1 ]
then
	echo "Arguments missing, required folder"
	exit 1
fi

cd $1

for folder in *_peass
do
	echo $folder | awk -F'[_]' '{print $2, ($3/300-1)*100" "}' | tr -d "\n"
	cat $folder/rca/treeMeasurementResults/*/MainTest/testMe.json \
		 | jq -r '.nodes.statistic | "\((.deviationOld / .meanOld * 1000000 | floor) / 1000000) \((.deviationCurrent / .meanCurrent * 1000000 | floor) / 1000000) \((.tvalue * 1000000 | floor) / 1000000)"'
done | sort -n -k 1,2 > relativeDeviations.csv 

for folder in *_peass
do
	java -cp $start/../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.precision.rca.analyze.GenerateRCAPrecisionPlot \
		-outlierRemoval \
		-data=$folder
	echo "Moving to "$folder"_results"
	mv results_outlierRemoval/ $folder"_results"
	cd $folder"_results"
	
	echo "Renaming (Fix for old Peass error, can be removed in the future)"
	for file in *
	do
		if [[ $file == *"().csv" ]]
		then
			filename=$(echo $file | awk '{print $2}' | tr -d "\(\)")
			mv "$file" $filename
		fi
	done
	
	mkdir graph-results
	getHeatmapData 13 graph-results
	
	gnuplot -c $start/plotHeatmap.plt graph-results/de.dagere.peass.MainTest_testMe.csv
	mv resultTemp.pdf ../$folder.pdf
	cd ..
done



cd $start 
