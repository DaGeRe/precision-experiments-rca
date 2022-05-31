for strategy in LEVELWISE COMPLETE UNTIL_SOURCE_CHANGE
do
	for depth in 2 4 6 8
	do
		for percentualChange in 1.010 1.020 1.030 1.050
		do
			heatmapFile=results/MERGED/$strategy/$depth"_"$percentualChange".pdf"
			cp $heatmapFile results/$strategy"_"$depth"_"$percentualChange.pdf
		done
	done
done
