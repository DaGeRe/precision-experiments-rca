#!/bin/bash

# To execute this script, please call GetMinimalFeasibleConfigurationSampling from precision-analysis first, for example using
# -data $HOME/sampling-vs-instrumentation-data/data-paper/instrumentation-usc
# -data $HOME/sampling-vs-instrumentation-data/data-paper/sampling-interval1
# -data $HOME/sampling-vs-instrumentation-data/data-paper/sampling-interval10
# -data $HOME/sampling-vs-instrumentation-data/data-paper/sampling-interval20

if [ $# -lt 1 ]
then
	echo "Arguments missing, requires folder"
	exit 1
fi

start=$(pwd)

cd $1

for depth in 2 4 6 8; do cat instrumentation-usc.csv | grep "^$depth" &> instrumentation-usc_$depth.csv; done
#for depth in 2 4 6 8; do cat instrumentation-complete.csv | grep "^$depth" &> instrumentation-complete_$depth.csv; done
for depth in 2 4 6 8; do cat sampling-interval1.csv | grep "^$depth" &> sampling-interval1_$depth.csv; done
for depth in 2 4 6 8; do cat sampling-interval10.csv | grep "^$depth" &> sampling-interval10_$depth.csv; done
for depth in 2 4 6 8; do cat sampling-interval20.csv | grep "^$depth" &> sampling-interval20_$depth.csv; done

gnuplot -c $start/plotRequiredVMs.plt
