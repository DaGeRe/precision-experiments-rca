package de.dagere.peass.precision.rca.analyze;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import de.dagere.peass.precision.rca.analyze.FindLowestIterationConfiguration.Values;

public class TestLowestConfigurationFinder {
   
   @Test
   public void testFindConfiguration() {
      List<Values> values = new LinkedList<>();
      values.add(new Values(1, 10, 99, "ttest"));
      values.add(new Values(2, 10, 98, "ttest"));
      values.add(new Values(3, 10, 98, "ttest"));
      
      values.add(new Values(1, 20, 98, "ttest"));
      values.add(new Values(2, 20, 99, "ttest"));
      values.add(new Values(3, 20, 98, "ttest"));
      
      values.add(new Values(1, 30, 98, "ttest"));
      values.add(new Values(2, 30, 99, "ttest"));
      values.add(new Values(3, 30, 99, "ttest"));
      
      values.add(new Values(1, 40, 99, "ttest"));
      values.add(new Values(2, 40, 99, "ttest"));
      values.add(new Values(3, 40, 99, "ttest"));
      
      Values minimum = FindLowestIterationConfiguration.getLowestConfiguration(values);
      Assert.assertEquals(2, minimum.vms);
      Assert.assertEquals(30, minimum.iterations);
   }
}
