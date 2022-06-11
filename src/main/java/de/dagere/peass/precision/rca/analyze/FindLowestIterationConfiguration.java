package de.dagere.peass.precision.rca.analyze;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;

/**
 * This finds the lowest iteration configuration that is possible for the given data
 * 
 * For the current dataset (https://zenodo.org/record/5995320#.YqSigTlBxhE), this does not make sense, as the main limiting factor was the time of the cluster for every root cause
 * analysis (which was 10 days), therefore, we were able to analyze up to a tree depth of 8 and LEVELWISE and COMPLETE were not able to finish for the tree depth of 8. In this
 * case, one should choose a strategy based on the expected place of the regression (if one excects it to be inside a changed node, UNTIL_SOURCE_CHANGE is fine) and the available
 * computation time.
 * 
 * It might make sense to use this class again if data of different tree structures or different strategies are evaluated.
 * 
 * @author DaGeRe
 *
 */
public class FindLowestIterationConfiguration {

   private static final int BOUNDARY_VALUE = 99;
   private static final Logger LOG = LogManager.getLogger(FindLowestIterationConfiguration.class);

   public static final class Values implements Comparable<Values> {
      final int vms;
      final int iterations;
      final double f1score;
      final String statisticalTest;

      public Values(final int vms, final int iterations, final double f1score, final String statisticalTest) {
         this.vms = vms;
         this.iterations = iterations;
         this.f1score = f1score;
         this.statisticalTest = statisticalTest;
      }

      @Override
      public int compareTo(final Values o) {
         int selfValue = vms * iterations;
         int otherValue = o.vms * o.iterations;
         return selfValue - otherValue;
      }

      @Override
      public String toString() {
         return "VMs: " + vms + " Iterations: " + iterations + " F1-Score: " + f1score;
      }

      public int getDuration() {
         return vms * iterations;
      }
   }

   public static void main(final String[] args) throws IOException {
      File folder = new File(args[0]);
      if (!folder.exists()) {
         throw new RuntimeException("Could not find " + folder);
      }
      String[] percentages = new String[] { "1.010", "1.020", "1.030", "1.050" };
      System.out.print("Strategie ");
      for (String percentage : percentages) {
         System.out.print(" & " + percentage);
      }
      System.out.println("\\\\");
      for (String strategy : new String[] { "COMPLETE", "UNTIL_SOURCE_CHANGE", "LEVELWISE" }) {
         // for (String strategy : new String[] { "UNTIL_SOURCE_CHANGE" }) {
         System.out.print("\\textbf{" + strategy.replace("_", "-") + "} & \\\\");

         File strategyfolder = new File(folder, strategy);
         String firstLine = "Iterationen ";
         String secondLine = "VMs ";
         for (String percentage : percentages) {

            Values percentageMinimum = null;
            for (String statisticalTest : new String[] { "", "_bimodal", "_mannWhitney" }) {
               Values statisticalTestMinimum = getCurrentTestMinimum(strategyfolder, percentage, statisticalTest);
               if (statisticalTestMinimum != null && statisticalTestMinimum.vms != Integer.MAX_VALUE) {
                  if (percentageMinimum == null || percentageMinimum.getDuration() > statisticalTestMinimum.getDuration()) {
                     percentageMinimum = statisticalTestMinimum;
                  }
               }
            }

            LOG.trace("Configuration for " + strategy + " and " + percentage);
            if (percentageMinimum != null && percentageMinimum.vms != Integer.MAX_VALUE) {
               firstLine += " & " + percentageMinimum.vms;
               secondLine += " &  " + percentageMinimum.iterations;
               LOG.trace(percentageMinimum.vms + " " + percentageMinimum.iterations + " " + percentageMinimum.f1score + " " + percentageMinimum.statisticalTest);
            } else {
               firstLine += " & X";
               secondLine += " & X";
               LOG.trace("null");
            }
         }
         System.out.println(firstLine + "\\\\");
         System.out.println(secondLine + "\\\\ \\hline");
      }
   }

   private static Values getCurrentTestMinimum(final File strategyfolder, final String percentage, final String statisticalTest) throws IOException {
      Values statisticalTestMinimum = null;

      for (int depth : new int[] { 2, 4, 6, 8 }) {
         File f1ScoreFile = new File(strategyfolder, depth + "_" + percentage + ".csv");
         // File f1ScoreFile = new File(f1scoreFile, percentage + "_outlierRemoval" + statisticalTest + File.separator + "de.peass.MainTest_testMe.csv");

         if (f1ScoreFile.exists() && f1ScoreFile.getName().endsWith(".csv")) {
            List<String> lines = Files.readLines(f1ScoreFile, StandardCharsets.UTF_8);

            List<Values> f1scores = new LinkedList<>();
            for (String line : lines) {
               String[] values = line.split(" ");
               if (values.length > 2) {
                  int vms = Integer.parseInt(values[0]);
                  int iterations = Integer.parseInt(values[1]);
                  double f1score = Double.parseDouble(values[2]);
                  f1scores.add(new Values(vms, iterations, f1score, statisticalTest));
               }
            }

            Values minimum = getLowestConfiguration(f1scores);
            LOG.trace(f1ScoreFile);
            if (minimum == null) { // if one depth / percentage combination does not find a value, we cannot find a suitable configuration
               LOG.trace("No combination for " + percentage + " " + statisticalTest + " " + f1ScoreFile.getName());
               statisticalTestMinimum = new Values(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, statisticalTest);
            } else {
               if (statisticalTestMinimum == null) {
                  statisticalTestMinimum = minimum;
               } else {
                  int newVMs = Math.max(minimum.vms, statisticalTestMinimum.vms);
                  int newIterations = Math.max(minimum.iterations, statisticalTestMinimum.iterations);
                  double newF1 = Math.min(minimum.f1score, statisticalTestMinimum.f1score);
                  statisticalTestMinimum = new Values(newVMs, newIterations, newF1, statisticalTest);
               }
            }
         } else {
            return new Values(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, statisticalTest);
         }
      }
      return statisticalTestMinimum;
   }

   public static Values getLowestConfiguration(final List<Values> f1scores) {
      final TreeSet<Values> candidates = new TreeSet<>();
      f1scores.forEach(value -> {
         if (value.f1score >= BOUNDARY_VALUE)
            candidates.add(value);
      });

      if (candidates.size() == 0) {
         LOG.trace("No candidates found");
      }

      Values minimum = null;
      for (Values candidate : candidates) {
         LOG.trace("Checking " + candidate);
         boolean noHigherWithLowerF1Score = true;
         for (Values check : f1scores) {
            if (check.f1score < BOUNDARY_VALUE && check.vms >= candidate.vms && check.iterations >= candidate.iterations) {
               noHigherWithLowerF1Score = false;
               LOG.trace("Broke by VMs {} Iterations {} F1-Score {}", check.vms, check.iterations, check.f1score);
               break;
            }
         }
         if (noHigherWithLowerF1Score) {
            minimum = candidate;
            break;
         }
      }
      LOG.trace("Found: {}", minimum);

      return minimum;
   }
}
