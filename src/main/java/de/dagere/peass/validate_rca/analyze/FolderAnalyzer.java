package de.dagere.peass.validate_rca.analyze;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peass.config.MeasurementConfiguration;
import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.utils.Constants;

abstract class FolderAnalyzer {

   protected final boolean useFullData;
   protected MeasurementConfiguration config;

   public FolderAnalyzer(final boolean useFullData) {
      this.useFullData = useFullData;
   }

   CauseSearchData getData(final File file, final boolean useDetailFile) throws JsonParseException, JsonMappingException, IOException {
      File dataFile = useDetailFile ? file.listFiles((FileFilter) new WildcardFileFilter("*.json"))[0]
            : new File(file, "details").listFiles((FileFilter) new WildcardFileFilter("*.json"))[0];
      CauseSearchData data = Constants.OBJECTMAPPER.readValue(dataFile, CauseSearchData.class);
      return data;
   }

   public abstract void processNode(File durationFolder, MeasuredNode node, CauseSearchData data);

   public abstract void print();

   public void printConfig() {
      if (config != null) {
         System.out.println("Config: Warmup: " + config.getWarmup() + " " + config.getIterations() + " " + config.getRepetitions() + " VMs: " + config.getVms());
      } else {
         System.out.println("Empty config");
      }
   }

   public void analyzeFolder(final File durationFolder, final File testClass) throws JsonParseException, JsonMappingException, IOException {
      if (useFullData) {
         File detailFolder = new File(testClass, "details");
         analyzeJSON(durationFolder, detailFolder);
      } else {
         analyzeJSON(durationFolder, testClass);
      }
   }

   private void analyzeJSON(final File durationFolder, final File testClass) throws IOException, JsonParseException, JsonMappingException {
      for (File measuredData : testClass.listFiles((FileFilter) new WildcardFileFilter("*.json"))) {
         CauseSearchData data = Constants.OBJECTMAPPER.readValue(measuredData, CauseSearchData.class);
         MeasuredNode node = data.getNodes();
         processNode(durationFolder, node, data);
      }
   }
}