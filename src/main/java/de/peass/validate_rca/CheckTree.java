package de.peass.validate_rca;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import de.peass.measurement.rca.data.CauseSearchData;
import de.peass.measurement.rca.serialization.MeasuredNode;
import de.peass.utils.Constants;
import de.peass.validate_rca.checking.Checker;
import de.peass.visualization.RCAFolderSearcher;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(description = "Checks whether the created result tree is correct", name = "check")
public class CheckTree implements Callable<Integer> {

   @Mixin
   private SlowerNodeInfos nodeInfos;

   @Option(names = { "-resultFolder", "--resultFolder" }, description = "File of the result folder", required = true)
   private File resultFolderFile;

   public static void main(String[] args) {
      final CheckTree command = new CheckTree();
      final CommandLine commandLine = new CommandLine(command);
      commandLine.execute(args);
   }

   @Override
   public Integer call() throws Exception {
      RCAFolderSearcher searcher = new RCAFolderSearcher(resultFolderFile);
      File rcaFile = searcher.searchRCAFiles().get(0);

      CauseSearchData data = Constants.OBJECTMAPPER.readValue(rcaFile, CauseSearchData.class);

      System.out.println("Checking tree change correctness...");
      
      Checker checker = new Checker(data, nodeInfos);
      checker.check();
      
      if (checker.getIncorrectlyChanged().size() > 0) {
         System.out.println("Incorrectly Changed");
         for (MeasuredNode incorrectlyChanged : checker.getIncorrectlyChanged()) {
            System.out.println(incorrectlyChanged.getCall());
         }
      }
      
      if (checker.getIncorrectlyUnchanged().size() > 0) {
         System.out.println("Incorrectly Unchanged");
         for (MeasuredNode incorrectlyUnchanged : checker.getIncorrectlyUnchanged()) {
            System.out.println(incorrectlyUnchanged.getCall());
         }
      }
      
      
      return null;
   }
}
