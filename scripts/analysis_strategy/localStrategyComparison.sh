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

function eventuallyAnalyze {
        inputFolder=$1
        outputFolder=$2
        if [ -d $inputFolder ]
        then
                plotStuff $inputFolder $outputFolder &
        else
                echo "Warning: Folder $inputFolder not existing"
        fi
}

BASEFOLDER=rawData
parallelFolder=$BASEFOLDER

mkdir results
for depth in 2 4 6 8
do
        for percent in 1.003 1.010 1.020
        do
                inputFolder=$parallelFolder/LEVELWISE/$depth/$depth"_LEVELWISE_"$percent"_ADD_1"/project_peass
                eventuallyAnalyze $inputFolder results/LEVELWISE_1/$depth/$percent
                inputFolder=$parallelFolder/LEVELWISE/$depth/$depth"_LEVELWISE_"$percent"_ADD_2"/project_peass
                eventuallyAnalyze $inputFolder results/LEVELWISE_2/$depth/$percent
                inputFolder=$parallelFolder/COMPLETE/$depth/$depth"_COMPLETE_"$percent"_ADD"/project_peass
                eventuallyAnalyze $inputFolder results/COMPLETE/$depth/$percent
                inputFolder=$parallelFolder/UNTIL_SOURCE_CHANGE/$depth/$depth"_UNTIL_SOURCE_CHANGE_"$percent"_ADD"/project_peass
                eventuallyAnalyze $inputFolder results/UNTIL_SOURCE_CHANGE/$depth/$percent

		echo "I am here"
		for strategy in LEVELWISE_1 LEVELWISE_2 COMPLETE UNTIL_SOURCE_CHANGE
		do
			echo "Test $strategy"
			resultfile=results/$strategy/$depth/$percent"_outlierRemoval"/depeassMainTest_testMe.pdf
			if [ -f $resultfile ]
			then
				cp $resultfile results/$strategy"_"$depth"_"$percent".pdf"
				echo "$resultfile copied to results/$strategy"_$depth"_"$percent".pdf"
			else
				echo "$resultfile not found and could not be copied"
			fi
		done
        done
done

#plotStuff $parallelFolder/COMPLETE/2/2_COMPLETE_0.3_ADD/project_peass COMPLETE/2/0.3 &
#plotStuff $parallelFolder/COMPLETE/2/2_COMPLETE_0.3_ADD/project_peass COMPLETE/2/0.3 &
#plotStuff $parallelFolder/COMPLETE/4/2_COMPLETE_0.3_ADD/ COMPLETE/4/0.3 &
#plotStuff $parallelFolder/COMPLETE/6/2_COMPLETE_0.3_ADD/ COMPLETE/6/0.3 &
#plotStuff /home/reichelt/daten3/diss/repos/precision-experiments-rca/sampling_comparison/sampling/03promille/project_3_peass LEVELWISE/3

#plotStuff $parallelFolder/UNTIL_SOURCE_CHANGE/9_8/*/project_9_peass parallel/UNTIL_SOURCE_CHANGE/9 &

wait

#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/COMPLETE/300_5_4_COMPLETE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ COMPLETE

#dataFolder=/home/reichelt/daten3/diss/repos/precision-experiments-rca/strategies/LEVELWISE/300_5_4_LEVELWISE_1.103/project_5_peass/
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder &> 1.txt
#java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.precision.rca.analyze.GenerateRCAPrecisionPlot -data $dataFolder -removeOutliers &> 2.txt
#./plotAll.sh $dataFolder/../ LEVELWISE
