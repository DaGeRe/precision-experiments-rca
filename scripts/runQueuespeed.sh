treedepth=1
vms=30

mvn -f .. clean install &> install.txt

for measuredNodes in 0 1 2 3 4 5 11
do
	export folder=project_$treedepth
	resultfolder=queuespeed_$measuredNodes/
	mkdir $resultfolder
	java -cp ../target/validate_rca-0.1-SNAPSHOT.jar de.peass.validate_rca.GenerateTreeExampleProject -treeDepth $treedepth -slowerLevel 0 \
		-slowParameter=1 -fastParameter=0 -childCount=10 \
		-type ADDITION \
		-out ../target/$folder &> "$resultfolder"generate.txt
		
	echo "Starting PRONTO $measuredNodes"
        java -cp $PEASS_PROJECT/distribution/target/peass-distribution-0.1-SNAPSHOT.jar de.peass.DependencyReadingStarter \
        	-folder ../target/$folder &> "$resultfolder"pronto.txt
	mv results $resultfolder
	
	version=$(cd ../target/project_1 && git rev-parse HEAD)

	echo "Starting Measurement"
        java -cp ../target/validate_rca-0.1-SNAPSHOT.jar de.peass.validate_rca.measurement.RunSomeNodeMeasurement \
        	    --folder=../target/$folder \
                    -dependencyfile $resultfolder/results/deps_$folder.json \
                    -vms $vms -nodeCount $measuredNodes -version $version \
		    -repetitions 100000 -iterations 10 &> "$resultfolder"measure.txt
	mv ../target/"$folder"_peass/ $resultfolder
        
done
