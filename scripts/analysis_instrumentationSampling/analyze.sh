#!/bin/bash

set -e

function getSum {
  awk -vOFMT=%.10g '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}

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

function getValueOverview {
	for folder in *_peass
	do
		echo $folder | awk -F'[_]' '{print $2, ($3/300-1)*100" "}' | tr -d "\n"
		cat $folder/rca/treeMeasurementResults/*/MainTest/testMe.json \
			 | jq -r '.nodes.statistic | "\((.deviationOld / .meanOld * 1000000 | floor) / 1000000) \((.deviationCurrent / .meanCurrent * 1000000 | floor) / 1000000) \((.tvalue * 1000000 | floor) / 1000000)"'
	done | sort -n -k 1,2 > relativeDeviations.csv 

	mkdir -p valueAnalysis

	for folder in *_peass
	do
		shortname=$(echo $folder | awk -F'[_]' '{print $2"_"($3/300-1)*100}' | tr -d "\n")
		cat $folder/rca/treeMeasurementResults/*/MainTest/details/testMe.json \
			| jq ".nodes.values.values" \
			| jq 'to_entries | map({key: .key, mean: (.value | map(.mean) | add / length)})' \
			| jq ".[] | .mean / 1000000" &> valueAnalysis/values_$shortname.csv
			
		cat $folder/rca/treeMeasurementResults/*/MainTest/details/testMe.json \
			| jq ".nodes.valuesPredecessor.values" \
			| jq 'to_entries | map({key: .key, mean: (.value | map(.mean) | add / length)})' \
			| jq ".[] | .mean / 1000000" &> valueAnalysis/predecessor_$shortname.csv

	#	echo $folder | awk -F'[_]' '{print $2, ($3/300-1)*100" "}' | tr -d "\n"
	#	cat valueAnalysis/values_$shortname.csv | getSum | tr "\n" " "
	#	cat valueAnalysis/predecessor_$shortname.csv | getSum
	done
}

start=$(pwd)

if [ $# -lt 1 ]
then
	echo "Arguments missing, required folder"
	exit 1
fi

cd $1

getValueOverview

for folder in *_peass
do
	java -cp $start/../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.precision.rca.analyze.GenerateRCAPrecisionPlot \
		-outlierRemoval \
		-data=$folder
	echo "Moving to "$folder"_results"
	mv results_outlierRemoval/ $folder"_results"
	cd $folder"_results"
	
	mkdir t-test
	getHeatmapData 9 t-test
	gnuplot -c $start/plotHeatmap.plt t-test/de.dagere.peass.MainTest_testMe.csv
	mv resultTemp.pdf ../"$folder"-t-test.pdf
	
	mkdir mann-whitney
	getHeatmapData 25 mann-whitney
	gnuplot -c $start/plotHeatmap.plt mann-whitney/de.dagere.peass.MainTest_testMe.csv
	mv resultTemp.pdf ../"$folder"-mann-whitney.pdf
	
	cd ..
done



cd $start 
