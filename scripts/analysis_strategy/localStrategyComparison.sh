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

function analyzeWorkload {
	baseFolder=$1
	depth=$2
	percent=$3
	workload=$4
	for strategy in LEVELWISE COMPLETE UNTIL_SOURCE_CHANGE
	do
		inputFolder=$baseFolder/$strategy/$depth/$depth"_"$strategy"_"$percent"_"$workload"_1"/project_peass
        	outputFolder=results/$strategy/$workload/$depth
        	eventuallyAnalyze $inputFolder $outputFolder/$percent
        
		echo "Test $strategy"
		resultfile=$outputFolder/$percent"_outlierRemoval"/depeassMainTest_testMe.pdf
		if [ -f $resultfile ]
		then
			goalFile=results/$strategy"_"$depth"_"$workload"_"$percent".pdf"
			cp $resultfile $goalFile
			echo "$resultfile copied to $goalFile"
		else
			echo "$resultfile not found and could not be copied"
		fi
	done
	wait
}

function createMergedData {
	depth=$1
	percent=$2
	
	for strategy in LEVELWISE COMPLETE UNTIL_SOURCE_CHANGE
	do 
		resultFolder=results/MERGED/$strategy
		mkdir -p $resultFolder
		folder=results/$strategy/$depth
		heatmaps=""
		if [ -f results/$strategy/ADD/$depth/"$percent"_noOutlierRemoval_mannWhitney/de.dagere.peass.MainTest_testMe.csv ]
		then
			heatmaps="results/$strategy/ADD/$depth/"$percent"_noOutlierRemoval_mannWhitney/de.dagere.peass.MainTest_testMe.csv"
		fi
		if [ -f results/$strategy/RAM/$depth/"$percent"_noOutlierRemoval_mannWhitney/de.dagere.peass.MainTest_testMe.csv ]
		then
			heatmaps="$heatmaps results/$strategy/RAM/$depth/"$percent"_noOutlierRemoval_mannWhitney/de.dagere.peass.MainTest_testMe.csv"
		fi
		if [ -f results/$strategy/SYSOUT/$depth/"$percent"_noOutlierRemoval_mannWhitney/de.dagere.peass.MainTest_testMe.csv ]
		then
			heatmaps="$heatmaps results/$strategy/SYSOUT/$depth/"$percent"_noOutlierRemoval_mannWhitney/de.dagere.peass.MainTest_testMe.csv"
		fi
		
		java -cp ../../target/precision-experiments-rca-0.1-SNAPSHOT.jar \
			de.precision.analysis.heatmap.MergeHeatmaps \
			$heatmaps
		mv result.csv $resultFolder/"$depth"_"$percent".csv
		
		gnuplot -c plotHeatmap.plt $resultFolder/"$depth"_"$percent".csv $resultFolder/"$depth"_"$percent".pdf
	done
}

if (( "$#" < 1 ))
then
	echo "Please pass folder that should be analyzed"
	exit 1
fi

baseFolder=$1

mkdir results
for depth in 2 4 6 8
do
        for percent in 1.010 1.020 1.030 1.050
        do
		for workload in ADD RAM SYSOUT
		do
			analyzeWorkload $baseFolder $depth $percent $workload
		done
		
		createMergedData $depth $percent
        done
done



#./plotAll.sh $dataFolder/../ LEVELWISE
