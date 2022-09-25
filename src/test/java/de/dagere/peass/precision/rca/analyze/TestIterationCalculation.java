package de.dagere.peass.precision.rca.analyze;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestIterationCalculation {

   private List<StatisticalSummary> oneVM = new LinkedList<>();

   @BeforeEach
   public void buildDataset() {
      oneVM.add(new DescriptiveStatistics(new double[] { 3.0, 3.0, 3.0 }));
      oneVM.add(new DescriptiveStatistics(new double[] { 2.0, 2.0, 2.0 }));
      oneVM.add(new DescriptiveStatistics(new double[] { 2.0, 1.0, 1.0 }));
      oneVM.add(new DescriptiveStatistics(new double[] { 0.5, 0.5, 0.5 }));
   }

   @Test
   public void testNormal() {
      Assert.assertEquals(2, SummaryInterpolator.getInterpolatedStatistics(oneVM, 6).getMean(), 0.01);
   }

   @Test
   public void testWarmupInterpolation() {
      Assert.assertEquals(8d / 5, SummaryInterpolator.getInterpolatedStatistics(oneVM, 9).getMean(), 0.01);
   }

   @Test
   public void testFinishInterpolation() {
      Assert.assertEquals(6.5 / 5, SummaryInterpolator.getInterpolatedStatistics(oneVM, 10).getMean(), 0.01);
   }
}
