function plotStuff {
	dataFolder=$1
	resultfolder=$2
	
	
	echo "Plotting $dataFolder"
	
	java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -alsoPlotChilds -removeOutliers &> 2.txt
	./plotAll.sh $dataFolder/../ $resultfolder
	echo
}

#sequential

BASEFOLDER=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies_v2

#plotStuff /home/reichelt/daten3/diss/repos/precision-experiments-rca/sampling_comparison/sampling/03promille/project_3_peass LEVELWISE/3

#plotStuff $BASEFOLDER/COMPLETE/3_2/*/project_3_peass COMPLETE/3
#plotStuff $BASEFOLDER/COMPLETE/5_4/*/project_5_peass COMPLETE/5

#plotStuff $BASEFOLDER/UNTIL_SOURCE_CHANGE/3_2/*/project_3_peass UNTIL_SOURCE_CHANGE/3
#plotStuff $BASEFOLDER/UNTIL_SOURCE_CHANGE/5_4/*/project_5_peass UNTIL_SOURCE_CHANGE/5
#plotStuff $BASEFOLDER/UNTIL_SOURCE_CHANGE/9_8/*/project_9_peass UNTIL_SOURCE_CHANGE/9

#parallel

#plotStuff $BASEFOLDER/parallel/COMPLETE/3_2/*/project_3_peass parallel/COMPLETE/3
#plotStuff $BASEFOLDER/parallel/COMPLETE/5_4/*/project_5_peass parallel/COMPLETE/5

#plotStuff $BASEFOLDER/parallel/UNTIL_SOURCE_CHANGE/3_2/*/project_3_peass parallel/UNTIL_SOURCE_CHANGE/3



#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/COMPLETE/300_5_4_COMPLETE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ COMPLETE

#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/LEVELWISE/300_5_4_LEVELWISE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ LEVELWISE
