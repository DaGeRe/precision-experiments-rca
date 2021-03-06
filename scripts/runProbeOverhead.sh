function createExecutionfile {
    version=$1
    file=$2
	echo '{"url" : "",  "versions" : {    "'$version'" : {      "testcases" : {        "de.peass.MainTest" : [ "testMe" ]      },      "predecessor" : "'$version'~1"    }  },  "android" : false}' > $file 
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
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
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
			--notUseSelectiveInstrumentation \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"3")
		echo "3 - Measuring with OperationExecutionRecord, CircularFifoQueue and Source Instrumentation"
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
			--useCircularQueue \
			--notUseSelectiveInstrumentation \
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
			--notUseSelectiveInstrumentation \
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
			--notUseSelectiveInstrumentation \
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
			--notUseSelectiveInstrumentation \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"7")
		echo "7 - Measuring with Kieker and ReducedOperationExecutionRecord, Source Instrumentation, Circular Queue and Selective Instrumentation"
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
	"8")
		echo "8 - Measuring with Kieker and ReducedOperationExecutionRecord, Source Instrumentation, and Selective Instrumentation"
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
	"10")
		echo "10 - Measuring with Kieker and ReducedOperationExecutionRecord, Source Instrumentation, and Sampling"
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
			--useSampling \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
	"11")
		echo "11 - Measuring with Kieker and ReducedOperationExecutionRecord, Source Instrumentation, and Selective Instrumentation, and Sampling"
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
			--useSampling \
			-test de.peass.MainTest#testMe &> $resultfolder/measurement.txt
		;;
    esac
}

vms=30
fastParameter=300
slowParameter=301
workload="ADD"

iterations=100
repetitions=100000

RCA_STRATEGY="COMPLETE"

echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"

case "$MEASURE" in
"0")
	parent=probeOverhead/pure
	;;
"1")
	parent=probeOverhead/operationExecutionRecord
	;;
"2")
	parent=probeOverhead/operationExecutionRecord_sourceInstrumentation
	;;
"3")
	parent=probeOverhead/operationExecutionRecord_circular_sourceInstrumentation
	;;
"4")
	parent=probeOverhead/reducedOperationExecutionRecord
	;;
"5")
	parent=probeOverhead/reducedOperationExecutionRecord_sourceInstrumentation
	;;
"6")
	parent=probeOverhead/reducedOperationExecutionRecord_circular_sourceInstrumentation
	;;
"7")
	parent=probeOverhead/reducedOperationExecutionRecord_circular_sourceInstrumentation_selective
	;;
"8")
	parent=probeOverhead/reducedOperationExecutionRecord_sourceInstrumentation_selective
	;;
"9")
	parent=probeOverhead/kieker_postCompileWeaving
	;;
"10")
	parent=probeOverhead/kieker_sampling
	;;
*)
	echo "MEASURE value not supported: $MEASURE"
	exit 1
	;;
esac

#for treedepth in 2 4 8 16 32 48 64 80 96 128 
for treedepth in 2 4 8 16 32 64 128
do
    folder=project_$treedepth
    slower=$((treedepth-1))
    echo "Treedepth: $treedepth Slower: $slower"
        
    resultfolder=$parent/probeOverhead_"$treedepth"_"$RCA_STRATEGY"
    mkdir -p $resultfolder
    
    rm -rf ../target/$folder*
    
    if [ ! $MEASURE -eq 9 ]
    then
        java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.validate_rca.GenerateTreeExampleProject \
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
    	java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.validate_rca.GenerateTreeExampleProject \
    		-treeDepth $treedepth -slowerLevel $slower \
    		-childCount 1 \
		-slowParameter=$slowParameter \
		-fastParameter=$fastParameter \
		-type $workload \
		-createBytecodeweavingEnvironment \
		-out ../target/$folder &> $resultfolder/generate.txt
		
	mvn install:install-file -Dfile=../src/main/resources/bytecodeWeaving/kieker-1.14-aspectj-minimal.jar -DgroupId=net.kieker-monitoring -DartifactId=kieker -Dversion=1.14 -Dpackaging=jar -Dclassifier=aspectj-minimal 
	rm ~/.KoPeMe/de.peass.validate_rca/ -rf
	for (( i=1; i<=$vms; i++))
	do
	  echo "Executing $i"
	  mvn -f ../target/$folder/pom.xml clean test &> $resultfolder/$i.txt
	done
	mv ~/.KoPeMe/de.peass.validate_rca/ $resultfolder
    fi
    
    
    
    mv ../target/"$folder"_peass/ $resultfolder/
done
