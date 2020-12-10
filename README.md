Precision Experiment RCA
===================== 

The goal of this project is to determine a measurement method which is capable of finding the root cause of a performance change using root cause analysis (RCA). The root cause of a performance change is the set of methods which cause the performance change. RCA is the process of identifying a root cause using measurement. With this project, you can define an performance change size and use experiments in order to determine how many vm starts and executions are needed to identify the root causes of a change.

The performance changes currently can be used for an addition and RAM workloads. Other workloads could be added.

# Before all tests

To be able to use all measurement methods, the following commands need to be run to locally install dependencies in the correct version:
- kieker:
- KoPeMe:
- peass:
TODO

# Test Execution

The following experiment types may be executed:
- examiniation of the measurement overhead for probes in an call tree containing only of single nodes
- execution of root cause analysis for given RCA strategies and configurations
These will be described in the following.

## Overhead Examination

When measuring the performance of single methods, these need to be instrumented (sampling-based techniques are not considered here). This instrumentation causes performance overhead. Additionally, the measurement itself causes overhead if it is activated (if Kiekers adaptive instrumentation feature is used, instrumentation may be activated and deactivated at runtime). It is possibe to instrument using AspectJ or to instrument using peass source instrumentation. The measurement may be done using Kieker's `OperationexecutionRecord` or the `ReducedOperationExecutionRecord`, which omits every information of the original record except start time, end time and method name (potentially increasing performance). Furthermore, as a baseline, measurement may be done with no instrumentation at all.

The experiments may be executed using `for val in 7 6 5 3 4 0 1; do export MEASURE=$val; ./runSingleNodeTree.sh; done`. By specification of the environment variable $MEASURE, the used measurement method is set. For details, see `scripts/runSingleNodeTree.sh`.


After successfull execution of the experiments, the results may be analyzed using `scripts/singlenode/analyzeSingleNodeAll.sh`. This may result in the following graph:

TODO

## RCA Strategy and Configuration

TODO
