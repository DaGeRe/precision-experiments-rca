for strategy in LEVELWISE COMPLETE UNTIL_SOURCE_CHANGE
do
	for size in 2 4 6 8
	do
		for percentualChange in 1.010 1.020 1.030 1.050
		do
			heatmapFile=results/$strategy/$size/"$percentualChange"_noOutlierRemoval_mannWhitney/dedagerepeassMainTest_testMe.pdf
			cp $heatmapFile results/$strategy"_"$size"_"$percentualChange.pdf
		done
	done
done
