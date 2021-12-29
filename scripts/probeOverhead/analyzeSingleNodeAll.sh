function getSum {
   awk '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}

function getFolder {
	MEASURE=$1
	case "$MEASURE" in
	"0")
		parent=pure
		;;
	"1")
		parent=operationExecutionRecord
		;;
	"2")
		parent=operationExecutionRecord_sourceInstrumentation
		;;
	"3")
		parent=operationExecutionRecord_sourceInstrumentation_circular
		;;
	"4")
		parent=operationExecutionRecord_sourceInstrumentation_selective
		;;
	"5")
		parent=durationRecord
		;;
	"6")
		parent=durationRecord_sourceInstrumentation
		;;
	"7")
		parent=durationRecord_sourceInstrumentation_circular
		;;
	"8")
		parent=durationRecord_sourceInstrumentation_aggregated
		;;
	"9")
        	parent=durationRecord_sourceInstrumentation_aggregated_selective
        	;;
	"10")
        	parent=kieker_postCompileWeaving
        	;;
	*)
		echo "MEASURE value not supported: $MEASURE"
		exit 1
		;;
	esac
	echo $parent
}

function generateEmptyCSVs {
	for folderIndex in 0 1 2 4 5 6 8 9
	do
		folder=$(getFolder $folderIndex )
		if [ -d $folder ]
		then
			echo -n "" > outputCSVs/$folder.csv
		fi
	done
}

function getWarmedupValues {
	folder=$1
	size=$2
	if [[ $folder == pure* ]]
	then
		for file in $folder/probeOverhead_"$size"_*/project*peass/measurementsFull/measurements/de.dagere.peass.MainTest/*/*/testMe_*xml
		do
			count=$(cat $file | grep "value start=" | wc -l)
			warmedUp=$(($count/2))
			cat $file | grep "value start=" | tail -n $warmedUp | awk -F'[<>]' '{print $3}' | getSum
		done | awk '{print $2}' | getSum | awk '{print $2/1000000" "$1/$2}'
	else
		for file in $folder/probeOverhead_"$size"_*/project*peass/rca/archived/*/de.dagere.peass.MainTest/testMe/*/0/testMe_*xml
		do
			count=$(cat $file | grep "value start=" | wc -l)
			warmedUp=$(($count/2))
			cat $file | grep "value start=" | tail -n $warmedUp | awk -F'[<>]' '{print $3}' | getSum
		done | awk '{print $2}' | getSum | awk '{print $2/1000000" "$1/$2}'
	fi
}


function generateMeasurementDurations {
	baseFolder=$1
	echo -n "" > outputCSVs/measurementDurations.csv
	sizes=$(ls $baseFolder | awk -F'_' '{print $2}' | sort -n)
	for size in $sizes
	do
		echo -n "$size " >> outputCSVs/measurementDurations.csv
		for folderIndex in {0..10}
		do	
			folder=$(getFolder $folderIndex )
			if [ -d $folder ]
			then
				getWarmedupValues $folder $size | tr "\n" " " >> outputCSVs/measurementDurations.csv
			fi
		done
		echo >> outputCSVs/measurementDurations.csv
	done
}

function generateMeasuredDurations {
	baseFolder=$1
	echo -n "" > outputCSVs/measuredDurations.csv
	sizes=$(ls $baseFolder | awk -F'_' '{print $2}' | sort -n)
	for size in $sizes
	do
		echo -n "$size " >> outputCSVs/measuredDurations.csv
		for folderIndex in {1..10}
		do	
			folder=$(getFolder $folderIndex )
			if [ -d $folder ]
			then
				# Since jq does not allow depth 128, the following lines get the values by grep; this assumes default formatting of the JSON
				meanOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | grep "nodes" -A 9 | grep "meanOld\|meanCurrent" | awk '{print $3}' | tr -d "," | getSum | awk '{print $2}')
				deviationOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | grep "nodes" -A 9 | grep "deviationOld\|deviationCurrent" | awk '{print $3}' | tr -d "," | getSum | awk '{print $2}')
				if (( $folderIndex == 9 )) || (( $folderIndex == 8 ))
				then
					repetitions=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | grep "repetitions" | tr -d "," | awk '{print $3}')
					meanOld=$(echo "$meanOld $repetitions" | awk '{print $1/$2}')
					deviationOld=$(echo "$deviationOld $repetitions" | awk '{print $1/$2}')
				fi
				#meanOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | jq ".nodes.statistic.meanOld")
				#deviationOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | jq ".nodes.statistic.deviationOld")
				echo -n $meanOld" "$deviationOld" " >> outputCSVs/measuredDurations.csv
			fi
		done
		echo >> outputCSVs/measuredDurations.csv
	done
}

function printMeasurementMeasuredComparison {
	for folderIndex in 0 1 2 4 5 6 8 9
	do	
		folder=$(getFolder $folderIndex )
		echo $folder
		if [ -d $folder ]
		then
			for file in $folder/probeOverhead_*
			do
				size=$(echo $file | awk -F'_' '{print $(NF-1)}')
				echo -n "$size "	
				getWarmedupValues $folder $size | tr "\n" " "
				if [ -f $file/project*peass/rca/tree/*/MainTest/testMe.json ] 
				then
					meanOld=$(cat $file/project*peass/rca/tree/*/MainTest/testMe.json | grep "nodes" -A 9 | grep "meanOld\|meanCurrent" | awk '{print $3}' | tr -d "," | getSum | awk '{print $2}')
					deviationOld=$(cat $file/project*peass/rca/tree/*/MainTest/testMe.json | grep "nodes" -A 9 | grep "deviationOld\|deviationCurrent" | awk '{print $3}' | tr -d "," | getSum | awk '{print $2}')
					echo $meanOld" "$deviationOld
				else
					echo
				fi
			done
		fi
	done
}

mkdir -p outputCSVs
mkdir -p outputPDFs

generateEmptyCSVs

for folderIndex in 0 1 2 4 5 6 8 9
do
        folder=$(getFolder $folderIndex )
        if [ -d $folder ]
        then
                baseFolder=$folder
                break
        fi
done


if [ ! -d $baseFolder ]
then
	echo "Base Folder $baseFolder does not exist; please make sure the baseline experiments have been finished correctly."
	exit 1
fi



generateMeasurementDurations $baseFolder

gnuplot -c plotMeasurementDuration.plt
gnuplot -c plotASE.plt

generateMeasuredDurations $baseFolder

gnuplot -c plotMeasuredDuration.plt

printMeasurementMeasuredComparison $baseFolder
