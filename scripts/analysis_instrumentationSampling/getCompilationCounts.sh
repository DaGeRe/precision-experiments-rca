#!/bin/bash

function getSum {
  awk -vOFMT=%.10g '{sum += $1; square += $1^2} END {print sqrt(square / NR - (sum/NR)^2)" "sum/NR" "NR}'
}

function getTValueJSON {
	file=$1
	commits=$(cat $file  | jq ".methods[0].datacollectorResults[0].chunks[0].results[].commit" | sort | uniq | tr -d "\"")
	commit1=$(echo $commits | awk '{print $1}')
	commit2=$(echo $commits | awk '{print $2}')
	
	values1=$(cat $file | jq ".methods[0].datacollectorResults[0].chunks[0].results[] | select(.commit==\""$commit1"\") | .value" | getSum)
	values2=$(cat $file | jq ".methods[0].datacollectorResults[0].chunks[0].results[] | select(.commit==\""$commit2"\") | .value" | getSum)
	echo "Values1: "$values1" Values2: "$values2
	
	deviation1=$(echo $values1 | awk '{print $1}' | awk '{ printf("%.2f\n",$1) }')	
	mean1=$(echo $values1 | awk '{print $2}' | awk '{ printf("%.2f\n",$1) }')	
	size1=$(echo $values1 | awk '{print $3}')	
	deviation2=$(echo $values2 | awk '{print $1}' | awk '{ printf("%.2f\n",$1) }')	
	mean2=$(echo $values2 | awk '{print $2}' | awk '{ printf("%.2f\n",$1) }')	
	size2=$(echo $values2 | awk '{print $3}')
	
	sizefactor=$(echo "sqrt ("$size1*$size2/"("$size1+$size2"))" | bc -l)
	weighteddeviation=$(echo "sqrt(("$deviation1*$deviation1"/2)+("$deviation2*$deviation2"/2))" | bc -l)
	effectSize=$(echo "($mean1-$mean2)/$weighteddeviation" | bc -l)
	tvalue=$(echo "$sizefactor*($mean1-$mean2)/$weighteddeviation" | bc -l)
	echo "T="$tvalue" Effectsize: $effectSize"
}

function getCompilations {
	for file in size_2_pure.csv size_4_pure.csv size_2_usc.csv size_4_usc.csv
	do
		if [ -f $file ]
		then
			rm $file
		fi
	done
	
	mkdir compilations

	for vm in {0..49}
	do
		file=measure_2_peass/logs/measureLogs/d972766943da3a0c062ca94b88562632a7b2dec4/de.dagere.peass.MainTest/testMe/vm_"$vm"_d68d4314b942424e671750affb0589e04337f571/log_de.dagere.peass.MainTest/testMe.txt
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' | wc -l >> size_2_pure.csv
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' > compilations/pure_2_$vm.txt
		
		file=measure_4_peass/logs/measureLogs/8024af37440cfb29230566d6b7aff0e94caeaa3f/de.dagere.peass.MainTest/testMe/vm_"$vm"_aefa7f951ce0ed8929c681334b95b8e3059ff93c/log_de.dagere.peass.MainTest/testMe.txt
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' | wc -l >> size_4_pure.csv
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' > compilations/pure_4_$vm.txt
		
		file=usc_2_peass/logs/rcaLogs/d972766943da3a0c062ca94b88562632a7b2dec4/de.dagere.peass.MainTest/testMe/0/vm_"$vm"_d68d4314b942424e671750affb0589e04337f571/log_de.dagere.peass.MainTest/testMe.txt
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' | wc -l >> size_2_usc.csv
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' > compilations/usc_2_$vm.txt
		
		file=usc_4_peass/logs/rcaLogs/8024af37440cfb29230566d6b7aff0e94caeaa3f/de.dagere.peass.MainTest/testMe/0/vm_"$vm"_aefa7f951ce0ed8929c681334b95b8e3059ff93c/log_de.dagere.peass.MainTest/testMe.txt
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' | wc -l >> size_4_usc.csv
		cat $file | grep '^\[DEBUG\][ ]\+[0-9]\+[ ]\+[0-9]\+' > compilations/usc_4_$vm.txt
	done
}

if [ ! -d compilations ]
then
	getCompilations
fi

echo "== Values =="
echo "Pure 2"
getTValueJSON measure_2_peass/measurementsFull/MainTest_testMe.json | grep "Values1"

echo "Pure 4"
getTValueJSON measure_4_peass/measurementsFull/MainTest_testMe.json | grep "Values1"

echo "Instrumentation (USC) 2"
getTValueJSON usc_2_peass/measurementsFull/MainTest_testMe.json | grep "Values1"

echo "Instrumentation (USC) 4"
getTValueJSON usc_4_peass/measurementsFull/MainTest_testMe.json | grep "Values1"


echo
echo "== Compilations =="
for file in size_2_pure.csv size_4_pure.csv size_2_usc.csv size_4_usc.csv
do
	echo $file
	cat $file | getSum
done



for type in usc pure
do
	echo $type
	for size in 2 4
	do
		for level in 0 1 2 3 4
		do
			echo -n "$level "
		done
		echo
		for level in 0 1 2 3 4
		do
			cat compilations/"$type"_"$size"_0.txt | awk '{if ($4=='$level') print $0}' | wc -l | tr "\n" " "
		done
		echo
		
		mkdir compilations_stage4_depth"$size"
		for vm in {0..49}
		do
			cat compilations/"$type"_"$size"_"$vm".txt | awk '{if ($4==4) print $5}' &> compilations_stage4_depth"$size"/$type"_"$vm.txt
		done
	done
done

for vm in {0..49}
do
	grep -Fv -x -f compilations_stage4_depth2/pure"_"$vm.txt compilations_stage4_depth2/usc"_"$vm.txt | sort &> compilations_stage4_depth2/$vm.txt
	grep -Fv -x -f compilations_stage4_depth4/pure"_"$vm.txt compilations_stage4_depth4/usc"_"$vm.txt | sort &> compilations_stage4_depth4/$vm.txt
done

