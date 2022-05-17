function getSum {
  awk '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}

for file in $1/deviation_*_ADD
do 
	depth=$(echo $file | awk -F'_' '{print $2}')
	echo -n "$depth "
	jar=/home/reichelt/nvme/workspaces/dissworkspace/precision-experiments-rca/target/precision-experiments-rca-0.1-SNAPSHOT.jar
	#java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | wc -l | tr "\n" " "  
	java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | awk '{print $2}' | getSum
done | sort -k 1n -k 4n > deviations-ADD.csv

for file in $1/deviation_*_RAM
do 
	depth=$(echo $file | awk -F'_' '{print $2}')
	echo -n "$depth "
	jar=/home/reichelt/nvme/workspaces/dissworkspace/precision-experiments-rca/target/precision-experiments-rca-0.1-SNAPSHOT.jar
	#java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | wc -l | tr "\n" " "  
	java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | awk '{print $2}' | getSum
done | sort  -k 1n -k 4n > deviations-RAM.csv


for file in $1/deviation_*_SYSOUT
do 
	depth=$(echo $file | awk -F'_' '{print $2}')
	echo -n "$depth "
	jar=/home/reichelt/nvme/workspaces/dissworkspace/precision-experiments-rca/target/precision-experiments-rca-0.1-SNAPSHOT.jar
	#java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | wc -l | tr "\n" " "  
	java -cp $jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $file/project_* | awk '{print $2}' | getSum
done | sort  -k 1n -k 4n > deviations-SYSOUT.csv


gnuplot -c plot.plt
