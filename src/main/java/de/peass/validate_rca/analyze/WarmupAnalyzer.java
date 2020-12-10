package de.peass.validate_rca.analyze;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import de.peass.dependency.execution.MeasurementConfiguration;
import de.peass.measurement.rca.data.CauseSearchData;
import de.peass.measurement.rca.serialization.MeasuredNode;
import de.peass.steadyStateNodewise.GetGraphs;
import de.peass.steadyStateNodewise.PerVMDeviationDeriver;

/**
 * Prints the iteration count where the standard deviation inside the VM falls below the inter-VM-deviation
 * @author reichelt
 *
 */
class WarmupAnalyzer extends FolderAnalyzer {

   private DescriptiveStatistics lessIterationDeviationThanVMDeviation = new DescriptiveStatistics();
   
   public WarmupAnalyzer() {
      super(true);
   }
   
   @Override
   public void processNode(File durationFolder, MeasuredNode node, CauseSearchData data) {
      System.out.println(durationFolder.getName() + " " + (!node.getChildren().isEmpty()));
      this.config = data.getMeasurementConfig();
      System.out.print("Slow ");
      new PerVMDeviationDeriver(node.getKiekerPattern(), node.getValues().getValues().values()).printDeviations();
      printIterationWarmupPoint(node.getValues().getValues().values());
//      GetGraphs.printValues(data, node.getValues());
      System.out.print("Fast ");
      new PerVMDeviationDeriver(node.getKiekerPattern(), node.getValuesPredecessor().getValues().values()).printDeviations();
      printIterationWarmupPoint(node.getValuesPredecessor().getValues().values());

   }
   
   public void printIterationWarmupPoint(Collection<List<StatisticalSummary>> values) {
      DescriptiveStatistics allVMMeans = getVMMeans(values);
      System.out.println(allVMMeans.getMean() + " " + allVMMeans.getStandardDeviation());
      
      DescriptiveStatistics lessDeviationIteration = new DescriptiveStatistics();
      for (List<StatisticalSummary> vmIterations : values) {
         DescriptiveStatistics deviationUntilIndex = new DescriptiveStatistics();
         int iteration;
         for (iteration = 0; iteration < vmIterations.size() - 1; iteration++) {
            deviationUntilIndex.addValue(vmIterations.get(iteration).getMean());
            if (iteration != 0 && deviationUntilIndex.getStandardDeviation() < allVMMeans.getStandardDeviation()) {
               break;
            }
         }
//         System.out.println(iteration + " " + deviationUntilIndex.getStandardDeviation() + " " + allVMMeans.getStandardDeviation());
         lessDeviationIteration.addValue(iteration);
      }
      lessIterationDeviationThanVMDeviation.addValue(lessDeviationIteration.getMean());
//      System.out.println(lessDeviationIteration.getMax() + " " + lessDeviationIteration.getMean());
//      System.out.println();
   }

   private DescriptiveStatistics getVMMeans(Collection<List<StatisticalSummary>> values) {
      DescriptiveStatistics allVMMeans = new DescriptiveStatistics();
      for (List<StatisticalSummary> vmStart : values) {
         DescriptiveStatistics vmStartStatistics = new DescriptiveStatistics();
         for (StatisticalSummary vmPart : vmStart) {
            vmStartStatistics.addValue(vmPart.getMean());
         }
         allVMMeans.addValue(vmStartStatistics.getMean());
      }
      return allVMMeans;
   }

   @Override
   public void print() {
      System.out.println("Mean iteration where deviation is smaller than vm deviation: " + lessIterationDeviationThanVMDeviation.getMean() + " / " + lessIterationDeviationThanVMDeviation.getMax());
   }

}