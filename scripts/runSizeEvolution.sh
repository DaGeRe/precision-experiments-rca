function createExecutionfile {
    version=$1
    file=$2
       echo '{"url" : "", 
              "versions" : {
                 "'$version~1'" : {},
                 "'$version'" : {"testcases" : {"de.dagere.peass.MainTest" : [ "testMe" ] }, "predecessor" : "'$version'~1"}
              },
              "android" : false}' > $file 

}

function measure {
	echo "Starting Measurement"
    
	vms=$1
	iterations=$2
	repetitions=$3
	propertyfolder=$4
	version=$5

	$PEASS_PROJECT/peass searchcause \
		--folder=../target/$folder -executionfile $resultfolder/results/execute_$folder.json \
		--propertyFolder $propertyfolder \
		--timeout=20 \
		--vms=$vms \
		--iterations=$iterations \
		--warmup=$iterations \
		--repetitions=$repetitions \
		--rcaStrategy=UNTIL_SOURCE_CHANGE \
		--commit=$version \
		-test de.dagere.peass.MainTest#testMe &> $resultfolder/measurement.txt
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

function generateMethodDiffFile {
	resultfolder=$1
	version=$2
	clazz=$3
	method=$4
	
	folder=$resultfolder/properties/methods/$version/$clazz
	mkdir -p $folder
	file=$folder/"$method"__main.txt
	
	touch $file
	
	fileOld=$folder/"$method"__old.txt
	
	touch $fileOld
}

# Since Peass executes the old and the current version, and these are equal for this experiments, twice the count of vms will be executed
vms=50
fastParameter=300
slowParameter=300
workload="ADD"

iterations=100
repetitions=100000

RCA_STRATEGY="UNTIL_SOURCE_CHANGE"

echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"

if [ "$#" -lt 1 ]
then
	treedepth=2
else
	treedepth=$1
fi

parent="sizeEvolution/"
rm -rf $parent/*
sync

folder=project_$treedepth
slower=$((treedepth-1))
echo "Treedepth: $treedepth Slower: $slower"

for nodeCount in 1 2 4 8
do
	resultfolder=$parent/deviation_"$treedepth"_"$nodeCount"_"$workload"
	mkdir -p $resultfolder

	rm -rf ../target/$folder*


	java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.validate_rca.GenerateTreeExampleProject \
		-treeDepth $treedepth -slowerLevel $slower \
		-childCount 2 \
		-slowParameter=$slowParameter \
		-fastParameter=$fastParameter \
		-type $workload \
		-out ../target/$folder &> $resultfolder/generate.txt
			
	echo "Creating PRONTO-results"
	version=$(cd ../target/$folder && git rev-parse HEAD)
	mkdir $resultfolder/results/
	createExecutionfile $version $resultfolder/results/execute_$folder.json
	
	propertiesFolder=$resultfolder/properties
	echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.MainTest" "testMe")
	if (( $nodeCount >= 2 ))
	then
		echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.C0_0" "method0")
		
	fi
	if (( $nodeCount >= 4 ))
	then
		echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.C0_0" "method1")
		echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.C1_0" "method0")
	fi
	
	if (( $nodeCount >= 8 ))
	then
		echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.C1_0" "method1")
		echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.C1_1" "method0")
		echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.C1_1" "method1")
		echo $(generateMethodDiffFile $resultfolder $version "de.dagere.peass.C1_0" "(init)")
	fi
	    
	echo $(measure $vms $iterations $repetitions $propertiesFolder $version)
	
	sync
	sleep 1
	
	mv ../target/"$folder"_peass/ $resultfolder/
	
	java -cp ../target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.precision.rca.analyze.GetRelativeStandardDeviation $resultfolder/"$folder"_peass

checkResultExistence $resultfolder
done



