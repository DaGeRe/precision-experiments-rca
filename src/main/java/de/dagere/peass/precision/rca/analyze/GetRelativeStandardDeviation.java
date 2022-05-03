package de.dagere.peass.precision.rca.analyze;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import de.dagere.peass.folders.CauseSearchFolders;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.measurement.statistics.data.TestcaseStatistic;
import de.dagere.peass.utils.Constants;

/**
 * Prints the relative standard deviation of every node of a tree; currently implemented as simple main, might be extended to use picocli etc. if the requirements get more complex
 * 
 * @author DaGeRe
 *
 */
public class GetRelativeStandardDeviation {
   public static void main(String[] args) throws StreamReadException, DatabindException, IOException {
      File file = new File(args[0]);
      if (file.getName().endsWith(PeassFolders.PEASS_POSTFIX)) {
         CauseSearchFolders folders = new CauseSearchFolders(file);
         for (File testcaseFile : folders.getRcaMethodFiles()) {
            CauseSearchData data = Constants.OBJECTMAPPER.readValue(testcaseFile, CauseSearchData.class);
            printNodeData(data.getNodes(), 0);
         }
      }
   }

   private static void printNodeData(MeasuredNode node, int level) {
      TestcaseStatistic statistic = node.getStatistic();
      double relativeDeviationCurrent = statistic.getDeviationCurrent() / statistic.getMeanCurrent();
      double relativeDeviationOld = statistic.getDeviationOld() / statistic.getMeanOld();
      double averageDeviation = (relativeDeviationCurrent + relativeDeviationOld)/2;
      
      System.out.println("Deviation: " + averageDeviation + " " + node.getCall() + " " + level);
      
      for (MeasuredNode child : node.getChildren()) {
         printNodeData(child, level + 1);
      }
   }
}
