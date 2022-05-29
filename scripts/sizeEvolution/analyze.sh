function getSum {
  awk '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}

for workload in ADD RAM SYSOUT
do
	echo "Analyzing $workload"
	
	echo "#TreeDepth DeviationOfRelativeStandardDeviation RelativeStandardDeviation AnalyzedNodeCount" > deviations-$workload.csv

	for file in $1/deviation_*_$workload
	do 
		depth=$(echo $file | awk -F'_' '{print $2}')
		echo -n "$depth "
		
		nodes=$(echo $file | awk -F'_' '{print $3}')
		jar=/home/reichelt/nvme/workspaces/dissworkspace/precision-experiments-rca/target/precision-experiments-rca-0.1-SNAPSHOT.jar
		#java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | wc -l | tr "\n" " "  
		
		if (($depth == 1)) && (($nodes == 4))
		then
			echo "NaN NaN 4"	
		elif (($depth == 1)) && (($nodes == 8))
		then
			echo "NaN NaN 8"
		else
			java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | awk '{print $2}' | getSum
		fi
	done | sort -k 1n -k 4n >> deviations-$workload.csv
done


gnuplot -c plot.plt
