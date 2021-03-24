package de.peass.validate_rca.analyze;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.peass.measurement.analysis.Relation;
import de.peass.measurement.rca.data.CauseSearchData;
import de.peass.measurement.rca.serialization.MeasuredNode;
import de.peass.statistics.StatisticUtil;
import de.peass.utils.Constants;

public class GenerateTable {

   private static Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<CauseSearchData>>>>> dataMap = new TreeMap<>();

   public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
      File folder = new File(args[0]);

      RCAReadUtil rcaReadUtil = RCAReadUtil.getDataMap(folder, false);
      dataMap = rcaReadUtil.getDataMap();
      printAllTable();
   }

   private static void printAllTable() {
      System.out.println("Duration Iterations Repetitions VMs Worked");
      dataMap.forEach((duration, durationEntry) -> {
         durationEntry.forEach((iterations, iterationEntry) -> {
            iterationEntry.forEach((repetitions, repetitionEntry) -> {
               repetitionEntry.forEach((vms, vmEntry) -> {
                  vmEntry.forEach((data) -> {
                     printIsWorking(duration, iterations, repetitions, vms, data);
                  });
               });
            });
         });
      });
   }

   private static void printIsWorking(Integer duration, Integer iterations, Integer repetitions, Integer vms, CauseSearchData data) {
      System.out.print(duration + " " + iterations + " " + repetitions + " " + vms
            + " " + !data.getNodes().getChildren().isEmpty());
      if (!data.getNodes().getChildren().isEmpty()) {
         System.out.print(" " + (data.getNodes().getStatistic().getMeanCurrent() > data.getNodes().getStatistic().getMeanOld()));
         MeasuredNode next = data.getNodes().getChildByPattern("public void de.peass.C0_0.method0()");
         System.out.print(" " + next.getStatistic().getTvalue());
         MeasuredNode next2 = next.getChildByPattern("public void de.peass.C1_0.method0()");
         if (next2 != null) {
            System.out.print(" " + next2.getStatistic().getTvalue());
         }
      }
      System.out.println();
   }

   
}
