package de.dagere.peass.precision.rca.analyze;

import java.io.File;

/**
 * Little helper script that checks whether a folder containing RCA-result-data is complete
 * 
 * @author DaGeRe
 *
 */
public class CheckCompleteness {
   public static void main(final String[] args) {
      File folder = new File(args[0]);

      String[] workloads = new String[] { "ADD", "RAM", "SYSOUT" };
      String[] strategies = new String[] { "UNTIL_SOURCE_CHANGE", "LEVELWISE", "COMPLETE" };
      String[] percentages = new String[] { "1.003", "1.010", "1.020", "1.030", "1.050" }; // Saved as Strings, since the string representation is essential here
      int[] levels = new int[] { 2, 4, 6, 8 };

      for (String workload : workloads) {
         for (String percentage : percentages) {
            for (String strategy : strategies) {
               for (int level : levels) {
                  File expectedFile = new File(folder, strategy + File.separator + level + File.separator + level + "_" + strategy + "_" + percentage + "_" + workload + "_1");
                  if (!expectedFile.exists()) {
                     System.out.println("Missing: " + strategy + " " + level + " " + percentage + " " + workload);
                     System.out.println("Folder: " + expectedFile.getAbsolutePath());
                  }
               }
            }
         }
      }
   }
}
