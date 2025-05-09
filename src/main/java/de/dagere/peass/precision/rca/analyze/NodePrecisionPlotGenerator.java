package de.dagere.peass.precision.rca.analyze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dagere.peass.config.StatisticsConfig;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.measurement.rca.serialization.MeasuredValues;
import de.dagere.peass.measurement.statistics.Relation;
import de.dagere.peass.measurement.statistics.bimodal.CompareData;
import de.precision.analysis.repetitions.ExecutionData;
import de.precision.analysis.repetitions.PrecisionComparer;
import de.precision.analysis.repetitions.PrecisionConfig;
import de.precision.analysis.repetitions.PrecisionWriter;
import de.precision.analysis.repetitions.StatisticalTestResult;
import de.precision.analysis.repetitions.StatisticalTests;
import de.precision.processing.repetitions.sampling.SamplingConfig;
import de.precision.processing.repetitions.sampling.SamplingExecutor;

public class NodePrecisionPlotGenerator {

   private static final Logger LOG = LogManager.getLogger(NodePrecisionPlotGenerator.class);

   private final int vmCount = 20;
   private final int iterationCount = 10;
   private final int repetitionsOfAnalysis;
   private final StatisticsConfig statisticsConfig;

   private final MeasuredNode node;
   private final Relation expectedRelation;

   private final PrecisionConfig precisionConfig;

   public NodePrecisionPlotGenerator(final MeasuredNode node, final Relation expectedRelation, final StatisticsConfig statisticsConfig, final int repetitionsOfAnalysis,
         PrecisionConfig precisionConfig) {
      this.statisticsConfig = statisticsConfig;
      this.node = node;
      this.expectedRelation = expectedRelation;
      this.repetitionsOfAnalysis = repetitionsOfAnalysis;
      this.precisionConfig = precisionConfig;
   }

   public void generate(final File resultFolder) {
      final File resultFile = new File(resultFolder, node.getCall().replace('#', '_') + ".csv");
      if (node.getStatistic().getCalls() > 0) {
         try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
            PrecisionWriter.writeHeader(writer, precisionConfig.getTypes());
            int fullVmCount = node.getPureVMs();
            LOG.info(node.getStatistic().getCalls() + " " + fullVmCount + " " + iterationCount);
            int stepsize = (int) Math.max(1, node.getStatistic().getCalls() / fullVmCount / iterationCount);
            LOG.info("Iteration step size: " + stepsize);
//            for (int iterations = stepsize * 1; iterations <= stepsize * iterationCount; iterations += stepsize) {
//               tryIterationCount(resultFolder, writer, 0, iterations);
//            }

            for (int iterations = stepsize * 1; iterations <= stepsize * iterationCount / 2; iterations += stepsize) {
               tryIterationCount(resultFolder, writer, iterations, iterations);
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void tryIterationCount(final File resultFolder, final BufferedWriter writer, final int warmup, final int iterations) throws IOException {
      final double[] aggregatedPredecessor = getIterationDurationArray(node.getValuesPredecessor(), warmup, iterations);
      final double[] aggregatedCurrent = getIterationDurationArray(node.getValues(), warmup, iterations);

      new HistogramWriter(aggregatedPredecessor, aggregatedCurrent, resultFolder, iterations, node.getCall().replace("#", "_")).write();

      final CompareData data_changed = new CompareData(aggregatedPredecessor, aggregatedCurrent);
      final CompareData data_equal = new CompareData(aggregatedPredecessor, aggregatedPredecessor);

      final int vmStepSize = node.getPureVMs() / vmCount;
      LOG.info("VM step size: {} Iterations: {} Warmup: {} ", vmStepSize, iterations, warmup); // Need at least 2 VMs for t test
      for (int vms = Math.max(2, vmStepSize); vms <= vmStepSize * vmCount; vms += vmStepSize) {
         final SamplingConfig config = new SamplingConfig(vms, "TestMe", repetitionsOfAnalysis);
         final PrecisionComparer comparer = new PrecisionComparer(statisticsConfig, precisionConfig);

         for (int i = 0; i < config.getSamplingExecutions(); i++) {
            executeComparison(data_changed, config, comparer, expectedRelation);
         }

         for (int i = 0; i < config.getSamplingExecutions(); i++) {
            executeComparison(data_equal, config, comparer, Relation.EQUAL);
         }

         writeResults(writer, warmup, iterations, vms, comparer);
      }
   }

   private void writeResults(final BufferedWriter writer, final int warmup, final int iterations,
         final int vms, final PrecisionComparer comparer) throws IOException {
      final ExecutionData metadata = new ExecutionData(vms, warmup, iterations, 1);
      final PrecisionWriter precisionWriter = new PrecisionWriter(comparer, metadata);
      final Map<StatisticalTests, Map<StatisticalTestResult, Integer>> results = comparer.getOverallResults().getResults();
      precisionWriter.writeTestcase(writer, results);
   }

   private void executeComparison(final CompareData data, final SamplingConfig config, final PrecisionComparer comparer, final Relation expected) {
      SamplingExecutor executor = new SamplingExecutor(config, data, comparer);
      executor.executeComparisons(expected);
   }

   private double[] getIterationDurationArray(final MeasuredValues values, final int warmup, final int iterations) {
      double[] aggregated = new double[values.getValues().size()];
      for (int i = 0; i < values.getValues().size(); i++) {
         List<StatisticalSummary> iterationData = values.getValues().get(i);
         StatisticalSummary summary = SummaryInterpolator.getInterpolatedStatistics(iterationData, iterations + warmup, warmup);
         aggregated[i] = summary.getMean();
         LOG.debug("Mean " + i + " " + aggregated[i]);
      }
      return aggregated;
   }
}
