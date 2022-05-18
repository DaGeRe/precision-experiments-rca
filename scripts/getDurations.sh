if [ $# -lt 1 ]
then
	echo "The first argument should be the path of a folder where COMPLETE, LEVELWISE and UNTIL_SOURCE_CHANGE-Strategy folders are present"
	exit 1
fi

echo "This script calculates the duration of the overall execution and of one VM execution for all existing result folders"

homeFolder=$1
cd $1

for strategy in COMPLETE LEVELWISE UNTIL_SOURCE_CHANGE
do
	echo $strategy
	pwd
	cd $strategy
	for size in 2 4 6 8
	do
		cd $size
		pwd
		if [ -f $size"_"$strategy"_1.010_ADD_1.tar" ]
		then
			tar -xf $size"_"$strategy"_1.010_ADD_1.tar"
			slowerLevel=$(($size-1))
			expectedFile=R_300_"$size"_"$slowerLevel"_"$strategy"_1.010_100_1000000_100_ADD_1_1/project_peass/rca/archived
			echo $expectedFile
			version=$( ls $expectedFile/ | head -n 1)
			#echo "File: $version"
			folder="$expectedFile/$version/de.dagere.peass.MainTest/testMe/$version/0"
			start=$(cat $folder/testMe_0_*.json | jq ".methods[0].datacollectorResults[0].results[0].date")
			end=$(cat $folder/testMe_99_*.json | jq ".methods[0].datacollectorResults[0].results[0].date")
			
			vmStart=$(cat $folder/testMe_0_*.json | jq ".methods[0].datacollectorResults[0].results[0].fulldata.values[0].startTime")
			vmEnd=$(cat $folder/testMe_0_*.json | jq ".methods[0].datacollectorResults[0].results[0].fulldata.values[-1].startTime")
			#echo "($end-$start)/1000/3600"
			duration=$( echo "($end-$start)/1000/3600" | bc )
			echo "Duration: $duration h"
			#echo "($vmEnd-$vmStart)/1000"
			vmDuration=$( echo "($vmEnd-$vmStart)/1000" | bc )
			echo "VM-Duration: $vmDuration s"
		else
			echo "File for $strategy $size not found"
		fi
		cd ..
	done
	cd $homeFolder
done
