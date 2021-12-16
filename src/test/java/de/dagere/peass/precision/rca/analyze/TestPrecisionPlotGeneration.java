package de.dagere.peass.precision.rca.analyze;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.junit.jupiter.api.Test;

import de.dagere.peass.config.StatisticsConfig;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.measurement.rca.serialization.MeasuredValues;
import de.dagere.peass.measurement.statistics.Relation;
import de.dagere.peass.measurement.statistics.data.TestcaseStatistic;

public class TestPrecisionPlotGeneration {

   private static final int ITERATIONS_PER_CHUNK = 10;
   private static final int VMS = 50;

   @Test
   public void testSimpleGeneration() {
      MeasuredNode node = new MeasuredNode("Test", "Test#test()", "Test#test()");
      MeasuredValues values = new MeasuredValues();
      Map<Integer, List<StatisticalSummary>> valueMap = new HashMap<Integer, List<StatisticalSummary>>();
      for (int i = 0; i < VMS; i++) {
         List<StatisticalSummary> vmList = Arrays
               .asList(new StatisticalSummary[] { new StatisticalSummaryValues(20 - i, 1, ITERATIONS_PER_CHUNK, 5, 5, 50),
                     new StatisticalSummaryValues(15 - i, 1, ITERATIONS_PER_CHUNK, 5, 5, 50),
                     new StatisticalSummaryValues(10 - i, 1, ITERATIONS_PER_CHUNK, 5, 5, 50),
                     new StatisticalSummaryValues(12 - i, 1, ITERATIONS_PER_CHUNK, 5, 5, 50),
                     new StatisticalSummaryValues(10 - i, 1, ITERATIONS_PER_CHUNK, 5, 5, 50) });
         valueMap.put(i, vmList);
      }

      values.setValues(valueMap);
      node.setStatistic(new TestcaseStatistic(new StatisticalSummaryValues(11, 1, 10, 5, 5, 50), new StatisticalSummaryValues(11, 1, 10, 5, 5, 50), ITERATIONS_PER_CHUNK * 5 * VMS, ITERATIONS_PER_CHUNK * 5 * VMS));
      node.setValues(values);
      node.setValuesPredecessor(values);
      File resultFolder = new File("target/results-temp");
      resultFolder.mkdirs();
      new NodePrecisionPlotGenerator(node, Relation.LESS_THAN, new StatisticsConfig(), 10).generate(resultFolder);
   }
}
