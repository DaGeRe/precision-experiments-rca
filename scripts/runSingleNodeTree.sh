vms=30
fastParameter=300
slowParameter=305
workload="ADD"

iterations=10
repetitions=100000

RCA_STRATEGY="LEVELWISE"

echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"

case "$MEASURE" in
"0")
	parent=singlenode/pure
	;;
"1")
	parent=singlenode/operationExecutionRecord
	;;
"2")
	parent=singlenode/operationExecutionRecord_sourceInstrumentation
	;;
"3")
	parent=singlenode/operationExecutionRecord_circular_sourceInstrumentation
	;;
"4")
	parent=singlenode/reducedOperationExecutionRecord
	;;
"5")
	parent=singlenode/reducedOperationExecutionRecord_sourceInstrumentation
	;;
"6")
	parent=singlenode/reducedOperationExecutionRecord_circular_sourceInstrumentation
	;;
"7")
	parent=singlenode/reducedOperationExecutionRecord_circular_sourceInstrumentation_selective
	;;
*)
	echo "MEASURE value not supported: $MEASURE"
	exit 1
	;;
esac

for treedepth in 2 10 100 
do
    folder=project_$treedepth
    slower=$((treedepth-1))
    echo "Treedepth: $treedepth Slower: $slower"
        
    resultfolder=$parent/singlenode_"$treedepth"_"$RCA_STRATEGY"
    mkdir -p $resultfolder
    
    rm -rf ../target/$folder*
    
    java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.validate_rca.GenerateTreeExampleProject \
    	-treeDepth $treedepth -slowerLevel $slower \
    	-childCount 1 \
		-slowParameter=$slowParameter \
		-fastParameter=$fastParameter \
		-type $workload \
		-out ../target/$folder &> $resultfolder/generate.txt
    echo "Starting PRONTO"
    $PEASS_PROJECT/peass select -folder ../target/$folder &> $resultfolder/pronto.txt
    mv results $resultfolder/
    
    echo "Starting Measurement"
   
    case "$MEASURE" in
	"0")
		echo "0 - Measuring Pure (without kieker)"
		$PEASS_PROJECT/peass measure \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		   ;;
	"1")
		echo "1 - Measuring with OperationExecutionRecord, LinkedBlockingQueue and AspectJ (default Kieker)"
    		$PEASS_PROJECT/peass searchcause \
			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=OPERATIONEXECUTION \
			-test de.peass.MainTest#testMe &> $resultfolder/rca.txt
			;;
	"2")
		echo "2 - Measuring with OperationExecutionRecord, LinkedBlockingQueue and Source Instrumentation"
    		$PEASS_PROJECT/peass searchcause \
			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=OPERATIONEXECUTION \
			--useSourceInstrumentation \
			-test de.peass.MainTest#testMe &> $resultfolder/rca.txt
		;;
	"3")
		echo "3 - Measuring with OperationExecutionRecord, Source Instrumentation and Selective Instrumentation"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=OPERATIONEXECUTION \
			--useSourceInstrumentation \
			--useSelectiveInstrumentation \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
 	"4")
		echo "4 - Measuring with ReducedOperationExecutionRecord, LinkedBlockingQueue and AspectJ"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=REDUCED_OPERATIONEXECUTION \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"5")
		echo "5 - Measuring with ReducedOperationExecutionRecord, LinkedBlockingQueue and Source Instrumentation"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=REDUCED_OPERATIONEXECUTION \
			--useSourceInstrumentation \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"6")
		echo "6 - Measuring with ReducedOperationExecutionRecord, CircularFifoQueue and Source Instrumentation"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=REDUCED_OPERATIONEXECUTION \
			--useSourceInstrumentation \
			--useCircularQueue \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"7")
		echo "7 - Measuring with Kieker and ReducedOperationExecutionRecord, Source Instrumentation, circular queue and selective instrumentation"
		$PEASS_PROJECT/peass searchcause \
    			--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
			--timeout=10 \
			--vms=$vms \
			--iterations=$iterations \
			--warmup=$iterations \
			--repetitions=$repetitions \
			--rcaStrategy=$RCA_STRATEGY \
			--record=REDUCED_OPERATIONEXECUTION \
			--useSourceInstrumentation \
			--useCircularQueue \
			--useSelectiveInstrumentation \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
    esac
    
    mv ../target/"$folder"_peass/ $resultfolder/
done
