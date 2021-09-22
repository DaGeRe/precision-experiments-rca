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
		parent=operationExecutionRecord_sourceInstrumentation_circular_selective
		;;
	"5")
		parent=reducedOperationExecutionRecord
		;;
	"6")
		parent=reducedOperationExecutionRecord_sourceInstrumentation
		;;
	"7")
		parent=reducedOperationExecutionRecord_sourceInstrumentation_circular
		;;
	"8")
		parent=reducedOperationExecutionRecord_sourceInstrumentation_circular_aggregated
		;;
	"9")
        	parent=reducedOperationExecutionRecord_sourceInstrumentation_circular_aggregated_selective
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

for folderIndex in {0..7}
do
	folder=$(getFolder $folderIndex )
	if [ -d $folder ]
	then
		echo -n "" > $folder.csv
	fi
done

baseFolder="pure"

if [ ! -f $baseFolder ]
then
	echo "Base Folder $baseFolder does not exist; please make sure the baseline experiments have been finished correctly."
	exit 1
fi

echo -n "" > durations.csv
sizes=$(ls $baseFolder | awk -F'_' '{print $2}' | sort -n)
for size in $sizes
do
	echo -n "$size " >> durations.csv
	for folderIndex in {0..10}
	do	
		folder=$(getFolder $folderIndex )
		if [ -d $folder ]
		then
			cat $folder/probeOverhead_"$size"_*/project*peass/measurementsFull/*xml | grep value | tr -d "<value/>" | getSum | awk '{print $2/1000000" "$1/$2}' | tr "\n" " " >> durations.csv
			cat $folder/probeOverhead_"$size"_*/project*peass/measurementsFull/*xml | grep value | tr -d "<value/>" | getSum | awk '{print $2/1000000" "$1/$2}' | tr "\n" " " >> $folder.csv
		fi
	done
	echo >> durations.csv
done

gnuplot -c plotSingleNodeTree.plt
gnuplot -c plotASE.plt


echo -n "" > pure_durations.csv
sizes=$(ls $baseFolder | awk -F'_' '{print $2}' | sort -n)
for size in $sizes
do
	echo -n "$size " >> pure_durations.csv
	for folderIndex in {1..10}
	do	
		folder=$(getFolder $folderIndex )
		if [ -d $folder ]
		then
			# Since jq does not allow depth 128, the following lines get the values by grep; this assumes default formatting of the JSON
			meanOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | grep "nodes" -A 9 | grep "meanOld\|meanCurrent" | awk '{print $3}' | tr -d "," | getSum | awk '{print $2}')
			deviationOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | grep "nodes" -A 9 | grep "deviationOld\|deviationCurrent" | awk '{print $3}' | tr -d "," | getSum | awk '{print $2}')
			#meanOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | jq ".nodes.statistic.meanOld")
			#deviationOld=$(cat $folder/probeOverhead_"$size"_*/project*peass/rca/tree/*/MainTest/testMe.json | jq ".nodes.statistic.deviationOld")
			echo -n $meanOld" "$deviationOld" " >> pure_durations.csv
		fi
	done
	echo >> pure_durations.csv
done

gnuplot -c plotPureDurations.plt


for folderIndex in {0..10}
do	
	folder=$(getFolder $folderIndex )
	echo $folder
	if [ -d $folder ]
	then
		for file in $folder/probeOverhead_*
		do
			size=$(echo $file | awk -F'_' '{print $(NF-1)}')
			echo -n "$size "	
			cat $file/project*peass/measurementsFull/*xml | grep "~1" -B 8 | grep value | tr -d "<value/>" | getSum | awk '{print $2/1000" "$1/$2}' | tr "\n" " "
			if [ -f $file/project*peass/rca/tree/*/MainTest/testMe.json ] 
			then
				meanOld=$(cat $file/project*peass/rca/tree/*/MainTest/testMe.json | jq ".nodes.statistic.meanOld")
				deviationOld=$(cat $file/project*peass/rca/tree/*/MainTest/testMe.json | jq ".nodes.statistic.deviationOld")
				echo $meanOld" "$deviationOld
			else
				echo
			fi
		done
	fi
done



