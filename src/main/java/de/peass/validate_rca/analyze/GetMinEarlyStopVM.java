package de.peass.validate_rca.analyze;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peass.measurement.rca.data.CauseSearchData;

public class GetMinEarlyStopVM {
   private static Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<CauseSearchData>>>>> dataMap = new TreeMap<>();

   public static void main(final String[] args) throws JsonParseException, JsonMappingException, IOException {
      File folder = new File(args[0]);

      RCAReadUtil rcaReadUtil = RCAReadUtil.getDataMap(folder, true);
      dataMap = rcaReadUtil.getDataMap();
      
      printMinEarlyStopVM();
   }

   /**
    * Prints the minimum early stop VM where the FIRST LEVEL has worked (=PartiallyWorkedVMs)
    */
   private static void printMinEarlyStopVM() {
      System.out.println("Duration Iterations PartiallyWorkedVMs VMs MinEarlyStopVM");
      dataMap.forEach((duration, durationEntry) -> {
         durationEntry.forEach((iterations, iterationEntry) -> {
            int partiallyWorked = 0;
            int overall = 0;
            for (Map.Entry<Integer, Map<Integer, List<CauseSearchData>>> repetitionEntry : iterationEntry.entrySet()) {
               for (Map.Entry<Integer, List<CauseSearchData>> vmEntry : repetitionEntry.getValue().entrySet()) {
                  for (CauseSearchData data : vmEntry.getValue()) {
                     overall++;
                     if (!data.getNodes().getChildren().isEmpty()) {
                        partiallyWorked++;
                     }
                  }
               }
            }
            System.out.println(duration + " " + iterations + " | " + partiallyWorked + " " + overall);
         });
      });
   }

}
