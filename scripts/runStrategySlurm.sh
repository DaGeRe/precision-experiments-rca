#!/bin/bash

####SBATCH --nodes=1
####SBATCH --exclusive
#SBATCH --cpu-freq=high-high
#SBATCH --cpus-per-task=24
#SBATCH --ntasks=1

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


# Java path needs to be adapted manually
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk/

export PATH=/home/sc.uni-leipzig.de/do820mize/maven/apache-maven-3.8.3/bin:$JAVA_HOME/bin:/usr/lib64/qt-3.3/bin:/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/home/sc.uni-leipzig.de/do820mize/pxz:/home/sc.uni-leipzig.de/do820mize/tar-1.29/bin/bin:/home/sc.uni-leipzig.de/do820mize/git/git-2.9.5/bin-wrappers

if [ -z "$percentualDiff" ]
then
	percentualDiff=0.3
fi

echo "TreeDepth: $nodes Slower: $slower Workload: $workload Repetitions: $repetitions VMs: $vms RCA_STRATEGY: $RCA_STRATEGY Diff: $percentualDiff Levels: $levels"

workloadsize=300
fastParameter=$workloadsize
slowDouble=$(echo "300*(1+$percentualDiff/100)+0.5" | bc -l)
slowParameter=${slowDouble%.*}

diff=$(echo "(1+$percentualDiff*0.01)" | bc -l)
echo "Diff: $diff"

echo "Slower Version: $slowParameter Faster Version: $fastParameter Type: $workload"

rm -rf /tmp/peass-temp/*
id=1
resultfolder=/tmp/peass-temp/R_"$workloadsize"_"$nodes"_"$slower"_"$RCA_STRATEGY"_"$diff"_"$iterations"_"$repetitions"_"$vms"_"$workload"_"$levels"_$id/
while [[ -d $resultfolder ]]
do
	id=$((id+1))
	resultfolder=/tmp/peass-temp/R_"$workloadsize"_"$nodes"_"$slower"_"$RCA_STRATEGY"_"$diff"_"$iterations"_"$repetitions"_"$vms"_"$workload"_"$levels"_$id/
done

mkdir -p $resultfolder
projectFolder=$resultfolder/project

startfolder=$(pwd)
cd $resultfolder

java -cp /home/sc.uni-leipzig.de/do820mize/precision-experiments-rca/target/precision-experiments-rca-0.1-SNAPSHOT.jar de.dagere.peass.validate_rca.GenerateTreeExampleProject \
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
    createExecutionfile $version $resultfolder/results/traceTestSelection_project.json
fi

echo "Starting Measurement"
$PEASS_PROJECT/peass searchcause \
	--folder=$projectFolder -executionfile $resultfolder/results/traceTestSelection_project.json \
	--commit=$version \
	--timeout=50 \
	--vms=$vms \
	--iterations=$iterations \
	--warmup=0 \
	--outlierFactor=0 \
	--repetitions=$repetitions \
	--rcaStrategy=$RCA_STRATEGY \
	--record=DURATION \
	--useCircularQueue \
	--statisticTest ANY_NO_AGNOSTIC \
	--measurementStrategy=PARALLEL \
	--levels $levels \
	--propertyFolder=$resultfolder/results/properties_project \
	-test de.dagere.peass.MainTest#testMe &> $resultfolder/rca.txt

echo "Measurement finished, moving result"

rcaResultBase=/home/sc.uni-leipzig.de/do820mize/rca-results/
rcaResultFolder=$rcaResultBase/$RCA_STRATEGY/$nodes/

relativePath=$(realpath --relative-to=/tmp/peass-temp $resultfolder)
echo "Running tar -czf /tmp/peass-temp/"$nodes".tar -C /tmp/peass-temp/ $relativePath"
ls /tmp/peass-temp/$relativePath
tarfile=/tmp/peass-temp/"$nodes"_"$RCA_STRATEGY"_"$diff"_"$workload"_"$levels".tar
tar -czf $tarfile -C /tmp/peass-temp/ $relativePath
echo "Created tar: $tarfile"
ls -lah $tarfile

previewFolder=$rcaResultFolder/"$nodes"_"$RCA_STRATEGY"_"$diff"_"$workload"_"$levels"/project_peass/rca/treeMeasurementResults/
mkdir -p $previewFolder
rsync -avz $resultfolder/project_peass/rca/treeMeasurementResults/ $previewFolder
mv $tarfile $rcaResultFolder
rm -rf $resultfolder
