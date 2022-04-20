function createExecutionfile {
    version=$1
    file=$2
	echo '{"url" : "",  "versions" : {    "'$version'" : {      "testcases" : {        "de.dagere.peass.MainTest" : [ "testMe" ]      },      "predecessor" : "'$version'~1"    }  },  "android" : false}' > $file 
}

if [ $# -eq 1 ]
then
	echo "Please pass 2 parameters (tree depth AND level of slower node) or 0 parameters (defaults to 5 2)"
	exit 1
fi
if [ $# -gt 1 ]
then
	nodes=$1
	slower=$2
else
	nodes=5
	slower=4
fi
if [ $# -gt 2 ]
then
	workload=$3
else
	workload="ADD"
fi
if [ $# -gt 3 ]
then
	iterations=$4
else
	iterations=100
fi
if [ $# -gt 4 ]
then
	repetitions=$5
else
	repetitions=1000000
fi
if [ $# -gt 5 ]
then
	vms=$6
else
	vms=400
fi

if [ "$RCA_STRATEGY" == "" ]
then
	RCA_STRATEGY="COMPLETE"
fi

mvn -f .. clean install &> install.txt
echo "Iterations: $iterations Repetitions: $repetitions VMs: $vms Strategy: $RCA_STRATEGY"
for workloadsize in 300
do
	percentualDiff="1.003"
    fastParameter=$workloadsize
    slowParameter=$(echo "$workloadsize*$percentualDiff+0.5" | bc | awk '{print int ($1)}')
    if [ "$workload" == "BUSY_WAITING" ]
    then
 	fastParameter=100000
	slowParameter=$(echo "100*(1000+$durationdiff)" | bc)
    fi
    echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"
    export folder=project_$nodes
    
    id=1
    resultfolder=strategy/"$workloadsize"_"$nodes"_"$slower"_"$RCA_STRATEGY"_"$percentualDiff"_"$iterations"_"$repetitions"_"$vms"_"$workload"_$id/
    while [[ -d $resultfolder ]]
    do
        id=$((id+1))
		resultfolder=strategy/"$workloadsize"_"$nodes"_"$slower"_"$RCA_STRATEGY"_"$percentualDiff"_"$iterations"_"$repetitions"_"$vms"_"$workload"_$id/
    done
    
    mkdir -p $resultfolder
    java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.validate_rca.GenerateTreeExampleProject \
    	-treeDepth $nodes -slowerLevel $slower \
		-slowParameter=$slowParameter \
		-fastParameter=$fastParameter \
		-type $workload \
		-out ../target/$folder &> $resultfolder/generate.txt
    
    version=$(cd ../target/$folder && git rev-parse HEAD)
    if [ "$RCA_STRATEGY" == "UNTIL_SOURCE_CHANGE" ]
    then
	    echo "Starting PRONTO"
	    $PEASS_PROJECT/peass select -folder ../target/$folder &> $resultfolder/pronto.txt
	    mv results $resultfolder/
    else
	    echo "Creating PRONTO-results"
	    mkdir $resultfolder/results/
	    createExecutionfile $version $resultfolder/results/execute_$folder.json
	fi
    
    echo "Starting Measurement"
    $PEASS_PROJECT/peass searchcause \
		--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
		--commit $version \
		--timeout=20 \
		--vms=$vms \
		--iterations=$iterations \
		--warmup=0 \
		--outlierFactor=0 \
		--repetitions=$repetitions \
		--rcaStrategy=$RCA_STRATEGY \
		--record=DURATION \
		--useCircularQueue \
		--useSampling \
		--statisticTest T_TEST \
		--measurementStrategy=PARALLEL \
		--propertyFolder=$resultfolder/results/properties_$folder \
		-test de.dagere.peass.MainTest#testMe &> $resultfolder/rca.txt
    mv ../target/"$folder"_peass/ $resultfolder/
   
    java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.validate_rca.CheckTree \
	    -treeDepth $nodes -slowerLevel $slower \
	    -resultFolder $resultfolder/"$folder"_peass/

done

