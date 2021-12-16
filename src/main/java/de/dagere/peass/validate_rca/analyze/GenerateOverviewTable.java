package de.dagere.peass.validate_rca.analyze;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.measurement.statistics.Relation;
import de.dagere.peass.measurement.statistics.StatisticUtil;

public class GenerateOverviewTable {

   

   private static Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<CauseSearchData>>>>> dataMap = new TreeMap<>();

   public static void main(final String[] args) throws JsonParseException, JsonMappingException, IOException {
      File folder = new File(args[0]);

      RCAReadUtil rcaReadUtil = RCAReadUtil.getDataMap(folder, false);
      dataMap = rcaReadUtil.getDataMap();
      
      printAggregatedTable();
   }

   private static void printAggregatedTable() {
      System.out.println("Duration Iterations PartiallyWorkedVMs WorkedFullyVMs VMs");
      dataMap.forEach((duration, durationEntry) -> {
         durationEntry.forEach((iterations, iterationEntry) -> {
            int partiallyWorked = 0;
            int worked = 0;
            int overall = 0;
            for (Map.Entry<Integer, Map<Integer, List<CauseSearchData>>> repetitionEntry : iterationEntry.entrySet()) {
               for (Map.Entry<Integer, List<CauseSearchData>> vmEntry : repetitionEntry.getValue().entrySet()) {
                  for (CauseSearchData data : vmEntry.getValue()) {
                     overall++;
                     if (!data.getNodes().getChildren().isEmpty()) {
                        partiallyWorked++;
                        MeasuredNode next = data.getNodes().getChildByPattern("public void de.peass.C0_0.method0()");
                        MeasuredNode next2 = next.getChildByPattern("public void de.peass.C1_0.method0()");
                        if (next2 != null) {
                           boolean noWrongDifference = checkHasNoDifference(data, next2);
                           if (noWrongDifference) {
                              worked++;
                           }
                        }
                        
                     }
                  }
               }
            }
            System.out.println(duration + " " + iterations + " | " + partiallyWorked + " " + worked + " " + overall);
         });
      });
   }

   private static boolean checkHasNoDifference(final CauseSearchData data, final MeasuredNode next2) {
      boolean noWrongDifference = true;
      for (MeasuredNode child : next2.getChildren()) {
         if (StatisticUtil.agnosticTTest(child.getStatistic().getStatisticsCurrent(), 
               child.getStatistic().getStatisticsOld(), 
               data.getMeasurementConfig()) == Relation.UNEQUAL) {
            noWrongDifference = false;
            System.out.println("Wrong difference: " + child);
         }
      }
      return noWrongDifference;
   }
}
