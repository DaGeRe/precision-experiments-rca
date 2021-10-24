package de.dagere.peass.validate_rca;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.junit.Test;

import de.dagere.peass.config.MeasurementConfig;
import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.measurement.rca.serialization.MeasuredValues;
import de.dagere.peass.validate_rca.analyze.EarlyStopIterationAnalyzer;

public class TestEarlyStop {

   final int VMS = 3;
   final int ITERATIONS = 5;

   @Test
   public void testStatisticCreation() {
      EarlyStopIterationAnalyzer analyzer = new EarlyStopIterationAnalyzer();

      Map<Integer, List<StatisticalSummary>> values = buildValues(1.0);
      Map<Integer, List<StatisticalSummary>> valuesPredecessor = buildValues(2.0);

      final MeasuredNode node = new MeasuredNode("ClassA#MethodA", "public void ClassA.MethodA()", null);
      node.setValues(new MeasuredValues());
      node.getValues().setValues(values);
      node.setValuesPredecessor(new MeasuredValues());
      node.getValuesPredecessor().setValues(valuesPredecessor);
      final CauseSearchData data = new CauseSearchData();
      data.setConfig(new MeasurementConfig(VMS));
      data.getMeasurementConfig().setIterations(ITERATIONS * 3);
      analyzer.processNode(new File("/dev/null"), node, data);

   }

   private Map<Integer, List<StatisticalSummary>> buildValues(final double start) {
      Map<Integer, List<StatisticalSummary>> values = new HashMap<>();
      for (int measuredVMs = 0; measuredVMs < VMS; measuredVMs++) {
         final LinkedList<StatisticalSummary> value = new LinkedList<>();
         for (int iterations = 0; iterations < ITERATIONS; iterations++) {
            value.add(new DescriptiveStatistics(new double[] { start, start + 0.9, start + (iterations) }));
         }
         values.put(measuredVMs, value);
      }
      return values;
   }
}
