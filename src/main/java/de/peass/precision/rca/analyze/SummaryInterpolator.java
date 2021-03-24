package de.peass.precision.rca.analyze;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SummaryInterpolator {

   private static final Logger LOG = LogManager.getLogger(SummaryInterpolator.class);

   /**
    * Calculates the warmed up values assuming *iterations* are performed (and the first half is discarded for warmup) based on arbitrary measurement aggregations from one VM
    * 
    * @param summaries
    * @param count
    * @return
    */
   public static StatisticalSummary getInterpolatedStatistics(final List<StatisticalSummary> summaries, final int count) {
      return getInterpolatedStatistics(summaries, count / 2, count / 2);
   }

   public static StatisticalSummary getInterpolatedStatistics(final List<StatisticalSummary> summaries, final int count, final int warmup) {
      int executions = 0;

      List<StatisticalSummary> warmedUpStatistics = new LinkedList<>();
      for (StatisticalSummary current : summaries) {
         executions += current.getN();

         LOG.trace("Point: " + executions + " Overall: " + count + " Warmup: " + warmup + " Vals: " + current.getN());
         if (executions > warmup) {
            if (executions <= count) {
               if (executions - current.getN() < warmup) {
                  addPartialWarmup(warmup, executions, warmedUpStatistics, current);
               } else {
                  warmedUpStatistics.add(current);
                  LOG.trace("Adding " + current.getN() + " times (fully): " + current.getMean());
               }
            } else if (executions - current.getN() < count) {
               addPartialAfterMeasurement(count, executions, warmedUpStatistics, current);
            }

         }
      }
      return AggregateSummaryStatistics.aggregate(warmedUpStatistics);
   }

   private static void addPartialAfterMeasurement(final int count, int executions, List<StatisticalSummary> warmedUpStatistics, StatisticalSummary current) {
      int addCount = executions - count - 1;
      final StatisticalSummaryValues shortendStatistics = new StatisticalSummaryValues(current.getMean(), current.getVariance(), addCount, current.getMax(),
            current.getMin(), current.getSum() * addCount / current.getN());
      warmedUpStatistics.add(shortendStatistics);
      LOG.trace("Adding " + addCount + " times (finish): " + current.getMean());
   }

   private static void addPartialWarmup(final int warmup, int executions, List<StatisticalSummary> warmedUpStatistics, StatisticalSummary current) {
      int addCount = executions - warmup;
      final StatisticalSummaryValues shortendStatistics = new StatisticalSummaryValues(current.getMean(), current.getVariance(), addCount, current.getMax(),
            current.getMin(), current.getSum() * addCount / current.getN());
      warmedUpStatistics.add(shortendStatistics);
      LOG.trace("Adding " + addCount + " times (warmup): " + current.getMean());
   }
}
