package de.peass.validate_rca.analyze;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class AnalyseRCADeviations {

   private static final String type = "EarlyStopIteration";

   public static void main(final String[] args) throws JsonParseException, JsonMappingException, IOException {

      File folder = new File(args[0]);

      final File[] listFiles = folder.listFiles((FileFilter) new WildcardFileFilter("duration_*"));

      if (listFiles.length > 0) {
         analyzeFolders(listFiles);
      } else {
         for (File type : folder.listFiles((FileFilter) DirectoryFileFilter.INSTANCE)) {
            System.out.println(type.getName());
            for (File iteration : type.listFiles()) {
               System.out.println(iteration.getName());
               final File[] listFiles2 = iteration.listFiles((FileFilter) new WildcardFileFilter("duration_*"));
               analyzeFolders(listFiles2);
            }
         }
      }
   }

   private static void analyzeFolders(final File[] listFiles) throws IOException, JsonParseException, JsonMappingException {
      FolderAnalyzer dataAnalyzer;
      if (type.equals("VM")) {
         dataAnalyzer = new VMAnalyzer();
      } else if (type.equals("Warmup")) {
         dataAnalyzer = new WarmupAnalyzer();
      } else if (type.equals("EarlyStopIteration")) {
         dataAnalyzer = new EarlyStopIterationAnalyzer();
      } else {
         throw new RuntimeException("Only Warmup or VM type allowed!");
      }
      Arrays.sort(listFiles);
      for (File durationFolder : listFiles) {
         for (File peassFolder : durationFolder.listFiles((FileFilter) new WildcardFileFilter("project_*_peass"))) {
            analyzeFolder(dataAnalyzer, durationFolder, peassFolder);
         }
      }

      System.out.println("Overall");
      dataAnalyzer.print();
      dataAnalyzer.printConfig();
      System.out.println();

   }

   private static void analyzeFolder(final FolderAnalyzer analyzer, final File durationFolder, final File peassFolder)
         throws IOException, JsonParseException, JsonMappingException {
      File rcaFolder = new File(peassFolder, "rca/tree/");
      File versionFolder = rcaFolder.listFiles()[0];
      File testClass = versionFolder.listFiles()[0];
      analyzer.analyzeFolder(durationFolder, testClass);
   }

}
