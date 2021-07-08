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

public class FindLowestIterationConfiguration {

   private static final int BOUNDARY_VALUE = 99;
   private static final Logger LOG = LogManager.getLogger(FindLowestIterationConfiguration.class);

   public static final class Values implements Comparable<Values> {
      final int vms;
      final int iterations;
      final double f1score;

      public Values(final int vms, final int iterations, final double f1score) {
         this.vms = vms;
         this.iterations = iterations;
         this.f1score = f1score;
      }

      @Override
      public int compareTo(final Values o) {
         int selfValue = vms * iterations;
         int otherValue = o.vms * o.iterations;
         return selfValue - otherValue;
      }

      @Override
      public String toString() {
         // TODO Auto-generated method stub
         return "VMs: " + vms + " Iterations: " + iterations + " F1-Score: " + f1score;
      }
   }

   public static void main(final String[] args) throws IOException {
      File folder = new File(args[0]);
      if (!folder.exists()) {
         throw new RuntimeException("Could not find " + folder);
      }
      String[] percentages = new String[] { "1.003", "1.010", "1.020" };
//       for (String strategy : new String[] { "COMPLETE", "UNTIL_SOURCE_CHANGE", "LEVELWISE_1", "LEVELWISE_2" }) {
      for (String strategy : new String[] { "UNTIL_SOURCE_CHANGE" }) {
         File strategyfolder = new File(folder, strategy);
         for (String percentage : percentages) {
            Values percentageMinimum = null;
            for (File depthFolder : strategyfolder.listFiles()) {
               File f1ScoreFile = new File(depthFolder, percentage + "_outlierRemoval" + File.separator + "de.peass.MainTest_testMe.csv");
               if (f1ScoreFile.exists()) {
                  List<String> lines = Files.readLines(f1ScoreFile, StandardCharsets.UTF_8);

                  List<Values> f1scores = new LinkedList<>();
                  for (String line : lines) {
                     String[] values = line.split(" ");
                     if (values.length > 2) {
                        int vms = Integer.parseInt(values[0]);
                        int iterations = Integer.parseInt(values[1]);
                        double f1score = Double.parseDouble(values[2]);
                        f1scores.add(new Values(vms, iterations, f1score));
                     }
                  }

                  Values minimum = getLowestConfiguration(f1scores);
                  LOG.trace(f1ScoreFile);
                  if (minimum == null) { // if one depth / percentage combination does not find a value, we cannot find a suitable configuration
                     percentageMinimum = new Values(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
                  } else {
                     if (percentageMinimum == null) {
                        percentageMinimum = minimum;
                     } else {
                        int newVMs = Math.max(minimum.vms, percentageMinimum.vms);
                        int newIterations = Math.max(minimum.iterations, percentageMinimum.iterations);
                        double newF1 = Math.min(minimum.f1score, percentageMinimum.f1score);
                        percentageMinimum = new Values(newVMs, newIterations, newF1);
                     }
                  }
               }
            }
            if (percentageMinimum != null && percentageMinimum.vms != Integer.MAX_VALUE) {
               LOG.info("Found configuration for " + strategy + " and " + percentage);
               LOG.info(percentageMinimum.vms + " " + percentageMinimum.iterations + " " + percentageMinimum.f1score);
            } else {
               LOG.info("Did not find configuration for " + strategy + " and " + percentage);
            }
         }
      }
   }

   public static Values getLowestConfiguration(final List<Values> f1scores) {
      final TreeSet<Values> candidates = new TreeSet<>();
      f1scores.forEach(value -> {
         if (value.f1score >= BOUNDARY_VALUE)
            candidates.add(value);
      });

      if (candidates.size() == 0) {
         LOG.debug("No candidates found");
      }

      Values minimum = null;
      for (Values candidate : candidates) {
         LOG.trace("Checking " + candidate);
         boolean noHigherWithLowerF1Score = true;
         for (Values check : f1scores) {
            if (check.f1score < BOUNDARY_VALUE && check.vms >= candidate.vms && check.iterations >= candidate.iterations) {
               noHigherWithLowerF1Score = false;
               LOG.debug("Broke by VMs {} Iterations {} F1-Score {}", check.vms, check.iterations, check.f1score);
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
