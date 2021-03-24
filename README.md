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

The experiments may be executed using TODO

After successfull execution of the experiments, the results may be analyzed using `./analyzeSingleNodeAll.sh`. This may result in the following graph:

TODO

## RCA Strategy and Configuration



Methods need to be distinguishable by their signature. If recursion occurs or equal methods are called in different parts of the tree, the measurements need to be distinguished or it needs to be asures that the performance of their respective executions is equal. This could be done by measuring the parents method as well and taking the position in the tree into account to distinguish methods. 

The performance of respective method execution needs to be gaussian distributed. 

It is assumed that only one method changed its performance (and possibly method calling these method). If two methods change their performance and the effects of the change overlay, different effects may happen.

= Approach =

We want to find what is the minimal time difference in the average execution times which is measurable in which tree depth. 

We test 3 variants:
- Measuring the whole tree
- Measuring one level at a time saving every execution
- Measuring one level at a time using aggregated data saving

and for every variant, which tree depth (10, 100, 1000, 10000) and which average execution time differences (1 ns, 10 ns, 100 ns, 1mikrosekunde, 10 mikrosekunden) is measurable.

In order to create distinguishable programs, we insert performance regressions of every size (1 ns, 10 ns, ..) to every tree size (10, 100, ..) at level 5, 25, 125, 625 and 3125 if present. 
