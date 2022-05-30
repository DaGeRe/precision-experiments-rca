function plotStuff {
        dataFolder=$1
        outputFolder=$2

        mkdir -p $outputFolder

        echo "Plotting $dataFolder"

        rm -rf $dataFolder/../results_* -rf \
                && \
                java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar \
                de.dagere.peass.precision.rca.analyze.GenerateRCAPrecisionPlot \
                -data $dataFolder \
                -outlierRemoval \
                &> $outputFolder"_outlierremoval".txt \
                && \
                java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar \
                de.dagere.peass.precision.rca.analyze.GenerateRCAPrecisionPlot \
                -data $dataFolder \
                &> $outputFolder"_nooutlierremoval".txt \
                && \
                ./plotAll.sh $dataFolder/../ $outputFolder

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

if (( "$#" < 1 ))
then
	echo "Please pass folder that should be analyzed"
	exit 1
fi

parallelFolder=$1

mkdir results
for depth in 2 4 6 8
do
        for percent in 1.003 1.010 1.020 1.030 1.050
        do
                #inputFolder=$parallelFolder/LEVELWISE/$depth/$depth"_LEVELWISE_"$percent"_ADD_1"/project_peass
                #eventuallyAnalyze $inputFolder results/LEVELWISE_1/$depth/$percent
                #inputFolder=$parallelFolder/LEVELWISE/$depth/$depth"_LEVELWISE_"$percent"_ADD_2"/project_peass
                #eventuallyAnalyze $inputFolder results/LEVELWISE_2/$depth/$percent
                #inputFolder=$parallelFolder/COMPLETE/$depth/$depth"_COMPLETE_"$percent"_ADD_1"/project_peass
                #eventuallyAnalyze $inputFolder results/COMPLETE/$depth/$percent
                #inputFolder=$parallelFolder/UNTIL_SOURCE_CHANGE/$depth/$depth"_UNTIL_SOURCE_CHANGE_"$percent"_ADD_1"/project_peass
                #eventuallyAnalyze $inputFolder results/UNTIL_SOURCE_CHANGE/$depth/$percent

		for strategy in LEVELWISE COMPLETE UNTIL_SOURCE_CHANGE
		do
			inputFolder=$parallelFolder/$strategy/$depth/$depth"_"$strategy"_"$percent"_ADD_1"/project_peass
                	eventuallyAnalyze $inputFolder results/$strategy/$depth/$percent
                
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
        wait
done



#./plotAll.sh $dataFolder/../ LEVELWISE
