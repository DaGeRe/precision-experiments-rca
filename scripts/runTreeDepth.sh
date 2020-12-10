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
	iterations=10
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
    fastParameter=$workloadsize
    slowParameter=$(echo "$workloadsize*1.003+0.5" | bc | awk '{print int ($1)}')
    if [ "$workload" == "BUSY_WAITING" ]
    then
 	fastParameter=100000
	slowParameter=$(echo "100*(1000+$durationdiff)" | bc)
    fi
    echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"
    export folder=project_$nodes
    resultfolder=duration_"$workloadsize"_"$nodes"_"$slower"_"$RCA_STRATEGY"/
    
    mkdir $resultfolder
    java -cp ../target/validate_rca-0.1-SNAPSHOT.jar de.peass.validate_rca.GenerateTreeExampleProject \
    	-treeDepth $nodes -slowerLevel $slower \
		-slowParameter=$slowParameter \
		-fastParameter=$fastParameter \
		-type $workload \
		-out ../target/$folder &> $resultfolder/generate.txt
    echo "Starting PRONTO"
    $PEASS_PROJECT/peass select -folder ../target/$folder &> $resultfolder/pronto.txt
    mv results $resultfolder/
    
    echo "Starting Measurement"
    $PEASS_PROJECT/peass searchcause \
		--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
		--timeout=10 \
		--vms=$vms \
		--iterations=$iterations \
		--warmup=$iterations \
		--repetitions=$repetitions \
		--rcaStrategy=$RCA_STRATEGY \
		-test de.peass.MainTest#testMe &> $resultfolder/rca.txt
    mv ../target/"$folder"_peass/ $resultfolder/
   
    java -cp ../target/validate_rca-0.1-SNAPSHOT.jar de.peass.validate_rca.CheckTree \
	    -treeDepth $nodes -slowerLevel $slower \
	    -resultFolder $resultfolder/"$folder"_peass/

done

