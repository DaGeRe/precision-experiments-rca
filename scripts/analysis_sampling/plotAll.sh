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

function createPlotableFile {
	output=$1
	folder=$2
	add=$3
	index=$4
	mkdir -p "$output"_$add
	#cd $folder/results"_$add"
	getHeatmapData $index  "$output"_"$add"
}

function plot {
	folder=$1
	
	for file in $(ls $folder | grep ".csv" | grep -v "_current_[0-9]*.csv" | grep -v "_predecessor_[0-9]*.csv")
	do
		outputName=$(echo $file | awk -F'.' '{$NF=""; print $0".pdf"}' | tr -d " ")
		echo "Plotting $file, moving to $outputName"
		gnuplot -c plotHeatmap.plt $folder/$file
		mv resultTemp.pdf $folder/$outputName
	done
}

if [ $# -lt 2 ]
then
	echo "Arguments missing, required folder and output name"
	exit 1
fi

start=$(pwd)

folder=$1
output=$start/$2

echo "Creating output folder $output"

#createPlotableFile $output $folder "noOutlierRemoval" 13

cd $folder/results_"outlierRemoval"
createPlotableFile $output $folder "outlierRemoval" 13
createPlotableFile $output $folder "outlierRemoval_bimodal" 17

createPlotableFile $output $folder "outlierRemoval_mannWhitney" 25

cd $start
#plot "$output"_noOutlierRemoval
plot "$output"_outlierRemoval
plot "$output"_outlierRemoval_bimodal
plot "$output"_outlierRemoval_mannWhitney
