package de.dagere.peass.validate_rca.analyze;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.inference.TTest;

import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.measurement.rca.serialization.MeasuredValues;

public class EarlyStopIterationAnalyzer extends FolderAnalyzer {

   int maxIterationPreservingResult = -1;

   public EarlyStopIterationAnalyzer() {
      super(true);
   }

   @Override
   public void processNode(final File durationFolder, final MeasuredNode node, final CauseSearchData data) {
      for (int iteration = 10; iteration <= data.getMeasurementConfig().getIterations()
            * data.getMeasurementConfig().getRepetitions(); iteration += data.getMeasurementConfig().getRepetitions() * 5) {
         DescriptiveStatistics vmValues = getValues(node.getValues(), iteration);
         DescriptiveStatistics vmPredecessorValues = getValues(node.getValuesPredecessor(), iteration);
         System.out.println(vmPredecessorValues.getMean() + " " + vmPredecessorValues.getStandardDeviation() +
               " " + vmValues.getMean() + " " + vmValues.getStandardDeviation() + " " +
               new TTest().tTest(vmValues, vmPredecessorValues) + " " + new TTest().t(vmValues, vmPredecessorValues));
         System.out.println(iteration + " " + new TTest().tTest(vmValues, vmPredecessorValues, data.getMeasurementConfig().getStatisticsConfig().getType2error()));
      }

      // node.getValues()
      // node.getValuesPredecessor()
   }

   private DescriptiveStatistics getValues(final MeasuredValues node, final int iteration) {
      DescriptiveStatistics vmPredecessorValues = new DescriptiveStatistics();
      for (Entry<Integer, List<StatisticalSummary>> measurements : node.getValues().entrySet()) {
         StatisticalSummaryValues predecessorStatistic = createPartialTimeseries(iteration, measurements.getValue());
         vmPredecessorValues.addValue(predecessorStatistic.getMean());
      }
      return vmPredecessorValues;
   }

   private StatisticalSummaryValues createPartialTimeseries(final int iteration, final List<StatisticalSummary> measurements) {
      int taken = 0;
      StatisticalSummaryValues currentStatistic = new StatisticalSummaryValues(0, 0, 0, 0, 0, 0);
      for (StatisticalSummary values : measurements) {
         // System.out.println(taken + " " + values.getN() + " " + iteration);
         taken += values.getN();
         if (taken < iteration) {
            // System.out.println("Add: " + values.getMean() + " " + taken + " / " + iteration);
            currentStatistic = AggregateSummaryStatistics.aggregate(Arrays.asList(new StatisticalSummary[] { currentStatistic, values }));
         } else {
            break;
         }
      }
      // System.out.println("Overall: " + currentStatistic.getMean());
      return currentStatistic;
   }

   @Override
   public void print() {
      System.out.println("Maximum iteration preserving result: ");

   }

}
