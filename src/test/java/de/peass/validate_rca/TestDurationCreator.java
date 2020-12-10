package de.peass.validate_rca;

import org.junit.Assert;
import org.junit.Test;


public class TestDurationCreator {
   
   @Test
   public void testDurationCreator() {
      DurationCreator creator = new DurationCreator(300, 301, false, 2, new SlowerNodeInfos(15, 14, 0));
      
      Assert.assertEquals(0, creator.getDuration(3, 3, 14)[0]);
      
      Assert.assertEquals(151, creator.getDuration(14, 0, 14)[0]);
      Assert.assertEquals(150, creator.getDuration(14, 0, 14)[1]);
      Assert.assertEquals(0, creator.getDuration(14, 1, 14)[0]);
   }
}
