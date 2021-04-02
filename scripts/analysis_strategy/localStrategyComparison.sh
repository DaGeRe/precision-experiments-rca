function plotStuff {
        dataFolder=$1
        resultfolder=$2

        mkdir -p $resultfolder

        echo "Plotting $dataFolder"

        rm -rf $dataFolder/../results_* -rf \
                && \
                java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar \
                de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot \
                -data $dataFolder \
                -removeOutliers -alsoPlotChilds \
                &> $resultfolder"_outlierremoval".txt \
                && \
                java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar \
                de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot \
                -data $dataFolder \
                -alsoPlotChilds \
                &> $resultfolder"_nooutlierremoval".txt \
                && \
                ./plotAll.sh $dataFolder/../ $resultfolder

        echo "Plotting $dataFolder finished"
        echo
}

#sequential

BASEFOLDER=rawData

#plotStuff /home/reichelt/daten3/diss/repos/precision-experiments-rca/sampling_comparison/sampling/03promille/project_3_peass LEVELWISE/3

plotStuff $BASEFOLDER/sequential/LEVELWISE/3_2/*/project_3_peass results/sequential/LEVELWISE/3 &

plotStuff $BASEFOLDER/sequential/COMPLETE/3_2/*/project_3_peass results/sequential/COMPLETE/3 &
plotStuff $BASEFOLDER/sequential/COMPLETE/5_4/*/project_5_peass results/sequential/COMPLETE/5 &

plotStuff $BASEFOLDER/sequential/UNTIL_SOURCE_CHANGE/3_2/*/project_3_peass results/sequential/UNTIL_SOURCE_CHANGE/3 &
plotStuff $BASEFOLDER/sequential/UNTIL_SOURCE_CHANGE/5_4/*/project_5_peass results/sequential/UNTIL_SOURCE_CHANGE/5 &
plotStuff $BASEFOLDER/sequential/UNTIL_SOURCE_CHANGE/9_8/*/project_9_peass results/sequential/UNTIL_SOURCE_CHANGE/9 &

#parallel

parallelFolder=$BASEFOLDER/parallel

plotStuff $parallelFolder/LEVELWISE/3_2/*/project_3_peass results/parallel/LEVELWISE/3 &
plotStuff $parallelFolder/LEVELWISE/5_4/*/project_5_peass results/parallel/LEVELWISE/5 &
plotStuff $parallelFolder/LEVELWISE/9_8/*/project_9_peass results/parallel/LEVELWISE/9 &

plotStuff $parallelFolder/COMPLETE/3_2/*/project_3_peass results/parallel/COMPLETE/3 &
plotStuff $parallelFolder/COMPLETE/5_4/*/project_5_peass results/parallel/COMPLETE/5 &

plotStuff $parallelFolder/UNTIL_SOURCE_CHANGE/3_2/*/project_3_peass results/parallel/UNTIL_SOURCE_CHANGE/3 &
plotStuff $parallelFolder/UNTIL_SOURCE_CHANGE/5_4/*/project_5_peass results/parallel/UNTIL_SOURCE_CHANGE/5 &
plotStuff $parallelFolder/UNTIL_SOURCE_CHANGE/9_8/*/project_9_peass results/parallel/UNTIL_SOURCE_CHANGE/9 &

wait


#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/COMPLETE/300_5_4_COMPLETE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ COMPLETE

#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/LEVELWISE/300_5_4_LEVELWISE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ LEVELWISE
