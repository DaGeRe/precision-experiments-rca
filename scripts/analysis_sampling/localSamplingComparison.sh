function plotStuff {
	dataFolder=$1
	resultfolder=$2
	
	java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
	java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
	./plotAll.sh $dataFolder/../ LEVELWISE

	mkdir $resultfolder
	mv LEVELWISE* $resultfolder
}

plotStuff /home/reichelt/daten3/diss/repos/precision-experiments-rca/sampling_comparison/no_sampling/15promille/project_3_peass no_sampling_15

plotStuff /home/reichelt/daten3/diss/repos/precision-experiments-rca/sampling_comparison/no_sampling/03promille/project_3_peass no_sampling_03

plotStuff /home/reichelt/daten3/diss/repos/precision-experiments-rca/sampling_comparison/sampling/15promille/project_3_peass sampling_15

plotStuff /home/reichelt/daten3/diss/repos/precision-experiments-rca/sampling_comparison/sampling/03promille/project_3_peass sampling_03

#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/COMPLETE/300_5_4_COMPLETE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ COMPLETE

#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/LEVELWISE/300_5_4_LEVELWISE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ LEVELWISE
