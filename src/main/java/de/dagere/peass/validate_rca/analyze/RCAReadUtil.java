package de.dagere.peass.validate_rca.analyze;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.utils.Constants;

public class RCAReadUtil {

   static String[] types = new String[] { "ADDITION", "RESERVE_RAM", "THROW", "BUSY_WAITING" };

   public static RCAReadUtil getDataMap(final File folder, final boolean readDetailData) throws IOException, JsonParseException, JsonMappingException {
      RCAReadUtil rcaReadUtil = new RCAReadUtil(readDetailData);
      rcaReadUtil.analyzeJobs(folder);
      for (String type : types) {
         File candidate = new File(folder, type);
         if (candidate.exists()) {
            rcaReadUtil.analyzeJobs(candidate);
         }
      }
      return rcaReadUtil;
   }

   Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<CauseSearchData>>>>> dataMap = new TreeMap<>();
   boolean readDetailData;

   public RCAReadUtil(final boolean readDetailData) {
      this.readDetailData = readDetailData;
   }

   public Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<CauseSearchData>>>>> getDataMap() {
      return dataMap;
   }

   public void analyzeJobs(final File folder) throws IOException, JsonParseException, JsonMappingException {
      for (File jobFolder : folder.listFiles()) {
         // System.out.println("Reading: " + jobFolder);
         if (jobFolder.isDirectory()) {
            for (File durationFolder : jobFolder.listFiles((FileFilter) new WildcardFileFilter("job_*"))) {
               analyzeDurations(durationFolder);
            }
            analyzeDurations(jobFolder);
         }
      }
   }

   private void analyzeDurations(final File jobFolder) throws IOException, JsonParseException, JsonMappingException {
      for (File durationFolder : jobFolder.listFiles((FileFilter) new WildcardFileFilter("duration_*"))) {
         File rcaFolder = new File(durationFolder, "rca/tree/");
         if (rcaFolder.exists()) {
            handleRCAFolder(durationFolder, rcaFolder);
         } else {
            for (File candidate : durationFolder.listFiles((FileFilter) new WildcardFileFilter("project_*"))) {
               File rcaFolder2 = new File(candidate, "rca/tree/");
               handleRCAFolder(durationFolder, rcaFolder2);
            }
         }
      }
   }

   private void handleRCAFolder(final File durationFolder, final File rcaFolder) throws IOException, JsonParseException, JsonMappingException {
      File versionFolder = rcaFolder.listFiles()[0];
      File testClassFolder = versionFolder.listFiles()[0];
      if (readDetailData) {
         File detailsFolder = new File(testClassFolder, "details");
         if (detailsFolder.exists()) {
            readData(durationFolder, detailsFolder);
         }
      } else {
         readData(durationFolder, testClassFolder);
      }
   }

   private void readData(final File durationFolder, final File dataFolder) throws IOException, JsonParseException, JsonMappingException {
      for (File measuredData : dataFolder.listFiles((FileFilter) new WildcardFileFilter("*.json"))) {
         CauseSearchData data = Constants.OBJECTMAPPER.readValue(measuredData, CauseSearchData.class);
         List<CauseSearchData> list = getCurrentList(durationFolder, data);
         list.add(data);
      }
   }

   private List<CauseSearchData> getCurrentList(final File durationFolder, final CauseSearchData data) {
      int duration = Integer.parseInt(durationFolder.getName().replace("duration_", ""));
      Map<Integer, Map<Integer, Map<Integer, List<CauseSearchData>>>> iterationMap = dataMap.get(duration);
      if (iterationMap == null) {
         iterationMap = new TreeMap<>();
         dataMap.put(duration, iterationMap);
      }
      Map<Integer, Map<Integer, List<CauseSearchData>>> repetitionMap = iterationMap.get(data.getMeasurementConfig().getIterations());
      if (repetitionMap == null) {
         repetitionMap = new TreeMap<>();
         iterationMap.put(data.getMeasurementConfig().getIterations(), repetitionMap);
      }
      Map<Integer, List<CauseSearchData>> vmMap = repetitionMap.get(data.getMeasurementConfig().getVms());
      if (vmMap == null) {
         vmMap = new TreeMap<>();
         repetitionMap.put(data.getMeasurementConfig().getVms(), vmMap);
      }
      List<CauseSearchData> list = vmMap.get(data.getMeasurementConfig().getRepetitions());
      if (list == null) {
         list = new LinkedList<>();
         vmMap.put(data.getMeasurementConfig().getRepetitions(), list);
      }
      return list;
   }
}
