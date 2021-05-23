#!/bin/bash

####SBATCH --nodes=1
####SBATCH --exclusive
#SBATCH --cpu-freq=high-high
#SBATCH --cpus-per-task=24
#SBATCH --ntasks=1

function createExecutionfile {
    version=$1
    file=$2
	echo '{"url" : "",  "versions" : {    "'$version'" : {      "testcases" : {        "de.peass.MainTest" : [ "testMe" ]      },      "predecessor" : "'$version'~1"    }  },  "android" : false}' > $file 
}


#export JAVA_HOME=/usr/jdk64/jdk1.8.0_112/
export PATH=/nfs/user/do820mize/maven/apache-maven-3.5.4/bin:/usr/jdk64/jdk1.8.0_112/bin/:/usr/lib64/qt-3.3/bin:/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/nfs/user/do820mize/pxz:/nfs/user/do820mize/tar-1.29/bin/bin:/nfs/user/do820mize/git/git-2.9.5/bin-wrappers

if [ -z "$percentualDiff" ]
then
	percentualDiff=0.3
fi

echo "Nodes: $nodes Slower: $slower Workload: $workload Repetitions: $repetitions VMs: $vms RCA_STRATEGY: $RCA_STRATEGY Diff: $percentualDiff"

workloadsize=300
fastParameter=$workloadsize
slowDouble=$(echo "300*(1+$percentualDiff/100)+0.5" | bc -l)
slowParameter=${slowDouble%.*}

echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"

rm -rf tmp/peass-temp/R_*
id=1
resultfolder=/tmp/peass-temp/R_"$workloadsize"_"$nodes"_"$slower"_"$RCA_STRATEGY"_"$percentualDiff"_"$iterations"_"$repetitions"_"$vms"_"$workload"_$id/
while [[ -d $resultfolder ]]
do
	id=$((id+1))
	resultfolder=/tmp/peass-temp/R_"$workloadsize"_"$nodes"_"$slower"_"$RCA_STRATEGY"_"$percentualDiff"_"$iterations"_"$repetitions"_"$vms"_"$workload"_$id/
done

mkdir -p $resultfolder
projectFolder=$resultfolder/project

startfolder=$(pwd)
cd $resultfolder

java -cp /home/sc.uni-leipzig.de/do820mize/precision-experiments-rca/target/precision-experiments-rca-0.1-SNAPSHOT.jar de.peass.validate_rca.GenerateTreeExampleProject \
        -treeDepth $nodes -slowerLevel $slower \
        -slowParameter=$slowParameter \
        -fastParameter=$fastParameter \
        -type $workload \
        -out $projectFolder &> $resultfolder/generate.txt

export PEASS_PROJECT=/home/sc.uni-leipzig.de/do820mize/peass
version=$(cd $projectFolder && git rev-parse HEAD)
if [ "$RCA_STRATEGY" == "UNTIL_SOURCE_CHANGE" ]
then
    echo "Starting PRONTO"
    $PEASS_PROJECT/peass select -folder $projectFolder &> $resultfolder/pronto.txt
    mv results $resultfolder/
else
    echo "Creating PRONTO-results"
    mkdir $resultfolder/results/
    createExecutionfile $version $resultfolder/results/execute_project.json
fi

echo "Starting Measurement"
$PEASS_PROJECT/peass searchcause \
	--folder=$projectFolder -executionfile $resultfolder/results/execute_project.json \
	--version=$version \
	--timeout=20 \
	--vms=$vms \
	--iterations=$iterations \
	--warmup=$iterations \
	--repetitions=$repetitions \
	--rcaStrategy=$RCA_STRATEGY \
	--record=REDUCED_OPERATIONEXECUTION \
	--useCircularQueue \
	--useSampling \
	--statisticTest ANY_NO_AGNOSTIC \
	--measurementStrategy=PARALLEL \
	--propertyFolder=$resultfolder/results/properties_project \
	-test de.peass.MainTest#testMe &> $resultfolder/rca.txt

echo "Measurement finished, moving result"

rcaResultBase=/home/sc.uni-leipzig.de/do820mize/rca-results/
rcaResultFolder=$rcaResultBase/$RCA_STRATEGY/$nodes/

relativePath=$(realpath --relative-to=/tmp/peass-temp $resultfolder)
echo "Running tar -czf /tmp/peass-temp/"$nodes".tar -C /tmp/peass-temp/ $relativePath"
ls $relativePath
tarfile=/tmp/peass-temp/"$nodes"_"$RCA_STRATEGY"_"$percentualDiff"_"$workload".tar
tar -czf $tarfile -C /tmp/peass-temp/ $relativePath
echo "Created tar: $tarfile"
ls -lah $tarfile

previewFolder=$rcaResultFolder/"$nodes"_"$RCA_STRATEGY"_"$percentualDiff"_"$workload"/tree/
mkdir -p $previewFolder
rsync -avz $resultfolder/project_peass/rca/tree/ $previewFolder
mv $tarfile $rcaResultFolder
rm -rf $resultfolder
