mkdir -p /nfs/user/do820mize/processlogs/precision-rca/
for strategy in LEVELWISE UNTIL_SOURCE_CHANGE COMPLETE
do
	for nodes in 2 4 6
	do
		slower=$((nodes-1))
		repetitions=1000000
		iterations=10
		vms=1000
		workload=ADD
		export RCA_STRATEGY=$strategy
		#sbatch --partition=galaxy-low-prio \
		logfolder=/nfs/user/do820mize/processlogs/precision-rca/$workload/
		mkdir -p $logfolder
		sbatch \
                --nice=1 \
                --time=10-0 \
                --output=$logfolder/"%j"_"%n".out \
		--export=repetitions=$repetitions,vms=$vms,RCA_STRATEGY=$strategy,iterations=$iterations,nodes=$nodes,slower=$slower,workload=$workload \
		 runStrategySlurm.sh
		echo "Log of $nodes $strategy goes to $logfolder"
	done
done 
