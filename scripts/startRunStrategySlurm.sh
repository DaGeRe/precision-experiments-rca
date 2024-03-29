mkdir -p /home/sc.uni-leipzig.de/do820mize/processlogs/precision-rca
for workload in ADD RAM SYSOUT
do
	for strategy in UNTIL_SOURCE_CHANGE LEVELWISE COMPLETE
	do
		for nodes in 2 4 6 8
		do
			for diff in 1.0 2.0 3.0 5.0
			do
			slower=$((nodes-1))
			repetitions=1000000
			iterations=100
			vms=100
			levels=1
			export RCA_STRATEGY=$strategy
			logfolder=/home/sc.uni-leipzig.de/do820mize/processlogs/precision-rca/$workload/
			mkdir -p $logfolder
			sbatch \
                		--nice=1 \
                		--time=10-0 \
                		--output=$logfolder/"%j"_"%n".out \
				--export=repetitions=$repetitions,vms=$vms,RCA_STRATEGY=$strategy,iterations=$iterations,nodes=$nodes,slower=$slower,workload=$workload,percentualDiff=$diff,levels=$levels \
				runStrategySlurm.sh
				echo "Log of $nodes $strategy $diff goes to $logfolder"
			done
		done
	done 
done
