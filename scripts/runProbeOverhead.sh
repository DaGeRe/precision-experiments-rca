function createExecutionfile {
    version=$1
    file=$2
	echo '{"url" : "",  "versions" : {    "'$version'" : {      "testcases" : {        "de.dagere.peass.MainTest" : [ "testMe" ]      },      "predecessor" : "'$version'~1"    }  },  "android" : false}' > $file 
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

function measure {
    echo "Starting Measurement"
    
    vms=$1
    iterations=$2
    repetitions=$3
   
    case "$MEASURE" in
	"0")
		echo "0 - Measuring Pure (without kieker)"
		$PEASS_PROJECT/peass measure \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		   ;;
	"1")
		echo "1 - Measuring with OperationExecutionRecord, AspectJ and LinkedBlockingQueue (default Kieker)"
    		$PEASS_PROJECT/peass searchcause \
			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=OPERATIONEXECUTION \
			--notUseSourceInstrumentation \
			--notUseSelectiveInstrumentation \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
			;;
	"2")
		echo "2 - Measuring with OperationExecutionRecord, Source Instrumentation and LinkedBlockingQueue"
    		$PEASS_PROJECT/peass searchcause \
			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=OPERATIONEXECUTION \
			--notUseSelectiveInstrumentation \
			--useExtraction \
			--enableAdaptiveInstrumentation \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
			# --enableAdaptiveInstrumentation makes the instrumentation slower, but is required for fair comparison to AspectJ
		;;
	"3")
		echo "3 - Measuring with OperationExecutionRecord, Source Instrumentation and CircularFifoQueue"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=OPERATIONEXECUTION \
			--useCircularQueue \
			--notUseSelectiveInstrumentation \
			--useExtraction \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"4")
		echo "4 - Measuring with OperationExecutionRecord, Source Instrumentation, LinkedBlockingQueue and selective Instrumentation"
		echo "First step: Source reading"
		$PEASS_PROJECT/peass select -folder ../target/$folder &> $resultfolder/rts.txt
		echo "Second step: Measurement"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
    			--propertyFolder results/properties_$folder \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=UNTIL_SOURCE_CHANGE \
			--record=OPERATIONEXECUTION \
			--useExtraction \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
 	"5")
		echo "5 - Measuring with DurationRecord, AspectJ and LinkedBlockingQueue"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=DURATION \
			--notUseSourceInstrumentation \
			--notUseSelectiveInstrumentation \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"6")
		echo "6 - Measuring with DurationRecord, Source Instrumentation and LinkedBlockingQueue"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=DURATION \
			--notUseSelectiveInstrumentation \
			--useExtraction \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"7")
		echo "7 - Measuring with DurationRecord, Source Instrumentation and CircularFifoQueue"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=DURATION \
			--useCircularQueue \
			--notUseSelectiveInstrumentation \
			--useExtraction \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"8")
		echo "8 - Measuring with Kieker and DurationRecord, Source Instrumentation, LinkedBlockingQueue and aggregated Writer"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=DURATION \
			--notUseSelectiveInstrumentation \
			--useExtraction \
			--useAggregation \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"9")
		echo "9 - Measuring with Kieker and DurationRecord, Source Instrumentation, LinkedBlockingQueue, aggregated Writer and Selective Instrumentation "
		echo "First step: Source reading"
		$PEASS_PROJECT/peass select -folder ../target/$folder &> $resultfolder/rts.txt
		echo "Second step: Measurement"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
    			--propertyFolder results/properties_$folder \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=UNTIL_SOURCE_CHANGE \
			--record=DURATION \
			--useExtraction \
			--useAggregation \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"10")
		echo "10 - Measuring with Kieker and Post Compile Weaving (Currently not supported!)"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=20 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=DURATION \
			--notUseSelectiveInstrumentation \
			-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
    esac
}

function checkResultExistence {
	resultFolder=$1
	
	expectedFiles="$resultfolder/project_*_peass/rca/tree/*/MainTest/testMe.json"
	expectedFile=( $expectedFiles )
	if [ ! -f "${expectedFile[0]}" ]
	then
		echo "Warning: $expectedFile did not exist"
	else
		potentialNaN=$(cat $resultfolder/project_*_peass/rca/tree/*/MainTest/testMe.json | grep -v tvalue | grep NaN)
		if [ ! -z "$potentialNaN" ]
		then
			echo "Warning: $expectedFile did contain NaN: "
			nanString=$(echo $potentialNaN | head)
			echo ${nanString:0:100}
		else
			potentialCallProblem=$(cat $resultfolder/project_*_peass/rca/tree/*/MainTest/testMe.json | grep "calls\"" | tr -d " " | uniq)
			callLength=$(echo $potentialCallProblem | wc -l)
			if [ ! $callLength -eq 1 ]
			then
				echo "Calls are differing: $potentialCallProblem"
			fi
		fi
	fi
}

# Since Peass executes the old and the current version, and these are equal for this experiments, twice the count of vms will be executed
vms=15
fastParameter=300
slowParameter=300
workload="ADD"

iterations=100
repetitions=10000

RCA_STRATEGY="COMPLETE"

echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"

folderName=$(getFolder $folderIndex $MEASURE)
parent="probeOverhead/$folderName"
rm -rf $parent/*
sync

#for treedepth in 2 4 8 16 32 48 64 80 96 128 
for treedepth in 2 4 8 16 32 64 128
do
    folder=project_$treedepth
    slower=$((treedepth-1))
    echo "Treedepth: $treedepth Slower: $slower"
        
    resultfolder=$parent/probeOverhead_"$treedepth"_"$RCA_STRATEGY"
    mkdir -p $resultfolder
    
    rm -rf ../target/$folder*
    
    if [ ! $MEASURE -eq 10 ]
    then
        java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.validate_rca.GenerateTreeExampleProject \
    		-treeDepth $treedepth -slowerLevel $slower \
    		-childCount 1 \
		-slowParameter=$slowParameter \
		-fastParameter=$fastParameter \
		-type $workload \
		-out ../target/$folder &> $resultfolder/generate.txt
			
		echo "Creating PRONTO-results"
		version=$(cd ../target/$folder && git rev-parse HEAD)
		mkdir $resultfolder/results/
    		createExecutionfile $version $resultfolder/results/execute_$folder.json
		    
		measure $vms $iterations $repetitions
    else
	java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.validate_rca.GenerateTreeExampleProject \
    		-treeDepth $treedepth -slowerLevel $slower \
    		-childCount 1 \
		-slowParameter=$slowParameter \
		-fastParameter=$fastParameter \
		-type $workload \
		-createBytecodeweavingEnvironment \
		-out ../target/$folder &> $resultfolder/generate.txt
		
	mvn install:install-file -Dfile=../src/main/resources/bytecodeWeaving/kieker-1.14-aspectj-minimal.jar -DgroupId=net.kieker-monitoring -DartifactId=kieker -Dversion=1.14 -Dpackaging=jar -Dclassifier=aspectj-minimal 
	rm ~/.KoPeMe/de.dagere.peass.validate_rca/ -rf
	for (( i=1; i<=$vms; i++))
	do
	  echo "Executing $i"
	  mvn -f ../target/$folder/pom.xml clean test &> $resultfolder/$i.txt
	done
	mv ~/.KoPeMe/de.dagere.peass.validate_rca/ $resultfolder
    fi
    
    mv ../target/"$folder"_peass/ $resultfolder/
    
    sync
    sleep 1

    checkResultExistence $resultfolder
done
