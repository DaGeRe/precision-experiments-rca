package de.peass.precision.rca.analyze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.peass.config.StatisticsConfiguration;
import de.peass.measurement.analysis.Relation;
import de.peass.measurement.rca.serialization.MeasuredNode;
import de.peass.measurement.rca.serialization.MeasuredValues;
import de.precision.analysis.repetitions.ExecutionData;
import de.precision.analysis.repetitions.PrecisionComparer;
import de.precision.analysis.repetitions.PrecisionWriter;
import de.precision.analysis.repetitions.bimodal.CompareData;
import de.precision.processing.repetitions.sampling.SamplingConfig;
import de.precision.processing.repetitions.sampling.SamplingExecutor;

public class NodePrecisionPlotGenerator {

   private static final Logger LOG = LogManager.getLogger(NodePrecisionPlotGenerator.class);

   private final int vmCount = 20;
   private final int iterationCount = 10;
   private final StatisticsConfiguration statisticsConfig;

   private final MeasuredNode node;
   private final Relation expectedRelation;

   public NodePrecisionPlotGenerator(final MeasuredNode node, final Relation expectedRelation, final StatisticsConfiguration statisticsConfig) {
      this.statisticsConfig = statisticsConfig;
      this.node = node;
      this.expectedRelation = expectedRelation;
   }

   public void generate(final File resultFolder) {
      final File resultFile = new File(resultFolder, node.getCall().replace('#', '_') + ".csv");
      if (node.getStatistic().getCalls() > 0) {
         try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile))) {
            PrecisionWriter.writeHeader(writer);
            int fullVmCount = node.getPureVMs();
            LOG.info(node.getStatistic().getCalls() + " " + fullVmCount + " " + iterationCount);
            int stepsize = (int) (node.getStatistic().getCalls() / fullVmCount / iterationCount);
            LOG.info("Iteration step size: " + stepsize);
            for (int iterations = stepsize; iterations <= stepsize * iterationCount; iterations += stepsize) {
               tryIterationCount(resultFolder, writer, iterations);
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void tryIterationCount(final File resultFolder, final BufferedWriter writer, final int iterations) throws IOException {
      final double[] aggregatedPredecessor = getIterationDurationArray(node.getValuesPredecessor(), iterations);
      final double[] aggregatedCurrent = getIterationDurationArray(node.getValues(), iterations);

      new HistogramWriter(aggregatedPredecessor, aggregatedCurrent, resultFolder, iterations, node.getCall().replace("#", "_")).write();

      final CompareData data_changed = new CompareData(aggregatedPredecessor, aggregatedCurrent);
      final CompareData data_equal = new CompareData(aggregatedPredecessor, aggregatedPredecessor);

      final int vmStepSize = node.getPureVMs() / vmCount;
      LOG.info("VM step size: " + vmStepSize + " Iterations: " + iterations);
      for (int vms = vmStepSize; vms <= vmStepSize * vmCount; vms += vmStepSize) {
         final SamplingConfig config = new SamplingConfig(vms, "TestMe", true, false, 1000);
         final PrecisionComparer comparer = new PrecisionComparer(config, statisticsConfig);

         for (int i = 0; i < config.getSamplingExecutions(); i++) {
            executeComparison(data_changed, config, comparer, expectedRelation);
         }

         for (int i = 0; i < config.getSamplingExecutions(); i++) {
            executeComparison(data_equal, config, comparer, Relation.EQUAL);
         }

         writeResults(writer, iterations, vms, comparer);
      }
   }

   private void writeResults(final BufferedWriter writer, final int iterations, final int vms, final PrecisionComparer comparer) throws IOException {
      final ExecutionData metadata = new ExecutionData(vms, iterations, iterations, 1);
      final PrecisionWriter precisionWriter = new PrecisionWriter(comparer, metadata);
      final Map<String, Map<String, Integer>> results = comparer.getOverallResults().getResults();
      precisionWriter.writeTestcase(writer, results.entrySet());
   }

   private void executeComparison(final CompareData data, final SamplingConfig config, final PrecisionComparer comparer, final Relation expected) {
      SamplingExecutor executor = new SamplingExecutor(config, statisticsConfig, data, comparer);
      executor.executeComparisons(expected);
   }

   private double[] getIterationDurationArray(final MeasuredValues values, final int iterations) {
      double[] aggregated = new double[values.getValues().size()];
      for (int i = 0; i < values.getValues().size(); i++) {
         StatisticalSummary summary = SummaryInterpolator.getInterpolatedStatistics(values.getValues().get(i), iterations, 0);
         aggregated[i] = summary.getMean();
         LOG.debug("Mean " + i + " " + aggregated[i]);
      }
      return aggregated;
   }
}
