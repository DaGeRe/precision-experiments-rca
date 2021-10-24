package de.dagere.peass.transform;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peass.folders.CauseSearchFolders;
import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.utils.Constants;

public class TransformWrongRCAData {
   public static void main(final String[] args) throws JsonParseException, JsonMappingException, IOException {
      File parentFolder = new File(args[0]);
      for (File strategyFolder : parentFolder.listFiles()) {
         for (File levelCountFile : strategyFolder.listFiles()) {
            for (File jobFolder : levelCountFile.listFiles()) {
               if (jobFolder.isDirectory()) {
                  File peassFolder = new File(jobFolder, "project_peass");
                  fixFolder(peassFolder);
               }
            }
         }
      }
   }

   private static void fixFolder(final File transformFolder) throws IOException, JsonParseException, JsonMappingException {
      System.out.println("Fixing: " + transformFolder);

      CauseSearchFolders folders = new CauseSearchFolders(transformFolder);
      for (File version : folders.getRcaTreeFolder().listFiles()) {
         File detailedJSON = new File(version, "MainTest/details/testMe.json");

         CauseSearchData data = Constants.OBJECTMAPPER.readValue(detailedJSON, CauseSearchData.class);
         System.out.println(data.getTestcase());

         // RCA-Experiments should contain warmup 0, since the analysis is performed afterwards
         data.getMeasurementConfig().setWarmup(0); 
         
         MeasuredNode node = data.getNodes();
         fixChildren(node);

         Constants.OBJECTMAPPER.writeValue(detailedJSON, data);

      }
   }

   private static void fixChildren(final MeasuredNode node) {
      fixNode(node);
      for (MeasuredNode child : node.getChildren()) {
         fixChildren(child);
      }
   }

   private static void fixNode(final MeasuredNode node) {
      fixValues(node.getValues().getValues());
      fixValues(node.getValuesPredecessor().getValues());
      if (node.getStatistic().getCalls() == 19990000000l) {
         node.getStatistic().setCalls(20000000000l);
      }
      if (node.getStatistic().getCallsOld() == 19990000000l) {
         node.getStatistic().setCallsOld(20000000000l);
      }
   }

   private static void fixValues(final Map<Integer, List<StatisticalSummary>> inputValues) {
      for (Entry<Integer, List<StatisticalSummary>> values : inputValues.entrySet()) {
         StatisticalSummary wrongWarmupSummary = values.getValue().get(0);
         if (wrongWarmupSummary.getN() == 1990000) {
            long n = 2000000;
            StatisticalSummary corrected = new StatisticalSummaryValues(wrongWarmupSummary.getMean(), wrongWarmupSummary.getVariance(), n,
                  wrongWarmupSummary.getMax(), wrongWarmupSummary.getMin(), n * wrongWarmupSummary.getMean());
            values.getValue().set(0, corrected);
         } else if (wrongWarmupSummary.getN() == 2990000) {
            long n = 3000000;
            StatisticalSummary corrected = new StatisticalSummaryValues(wrongWarmupSummary.getMean(), wrongWarmupSummary.getVariance(), n,
                  wrongWarmupSummary.getMax(), wrongWarmupSummary.getMin(), n * wrongWarmupSummary.getMean());
            values.getValue().set(0, corrected);
         } else if (wrongWarmupSummary.getN() == 990000) {
            long n = 1000000;
            StatisticalSummary corrected = new StatisticalSummaryValues(wrongWarmupSummary.getMean(), wrongWarmupSummary.getVariance(), n,
                  wrongWarmupSummary.getMax(), wrongWarmupSummary.getMin(), n * wrongWarmupSummary.getMean());
            values.getValue().set(0, corrected);
         } else if (wrongWarmupSummary.getN() == 2000000 || wrongWarmupSummary.getN() == 3000000 || wrongWarmupSummary.getN() == 1000000) {
            // already fixed
         } else {
            throw new RuntimeException("Unexpected value: " + wrongWarmupSummary.getN());
         }
      }
   }
}
