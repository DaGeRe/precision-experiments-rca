package de.peass.validate_rca;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.peass.measurement.rca.data.CauseSearchData;
import de.peass.utils.Constants;
import de.peass.validate_rca.checking.Checker;

public class TestTreeCheck {
   
   public static final File TESTRESULT_FOLDER = new File("src/test/resources/testresults/");
   
   @Test
   public void testCorrectTree() throws JsonParseException, JsonMappingException, IOException {
      File testFile = new File(TESTRESULT_FOLDER, "correctResult.json");
      CauseSearchData data = Constants.OBJECTMAPPER.readValue(testFile, CauseSearchData.class);
      SlowerNodeInfos infos = new SlowerNodeInfos(3, 2, 0);
      
      final Checker checker = new Checker(data, infos);
      boolean result = checker.check();
      Assert.assertTrue(result);
   }
   
   @Test
   public void testWrongNodeIdentified() throws JsonParseException, JsonMappingException, IOException {
      File testFile = new File(TESTRESULT_FOLDER, "wrongNodeIdentified.json");
      CauseSearchData data = Constants.OBJECTMAPPER.readValue(testFile, CauseSearchData.class);
      SlowerNodeInfos infos = new SlowerNodeInfos(3, 2, 0);
      
      final Checker checker = new Checker(data, infos);
      boolean result = checker.check();
      Assert.assertFalse(result);
   }
   
   @Test
   public void testNodeChangeMissing() throws JsonParseException, JsonMappingException, IOException {
      File testFile = new File(TESTRESULT_FOLDER, "nodeMissing.json");
      CauseSearchData data = Constants.OBJECTMAPPER.readValue(testFile, CauseSearchData.class);
      SlowerNodeInfos infos = new SlowerNodeInfos(3, 2, 0);
      
      final Checker checker = new Checker(data, infos);
      boolean result = checker.check();
      Assert.assertFalse(result);
   }
}
