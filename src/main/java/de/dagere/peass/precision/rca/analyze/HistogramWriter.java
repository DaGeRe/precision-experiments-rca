package de.dagere.peass.precision.rca.analyze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HistogramWriter {

   private final double[] aggregatedPredecessor, aggregatedCurrent;
   private final File resultFolder;
   private final int iterations;
   private final String call;


   public HistogramWriter(final double[] aggregatedPredecessor, final double[] aggregatedCurrent, final File resultFolder, final int iterations, final String call) {
      this.aggregatedPredecessor = aggregatedPredecessor;
      this.aggregatedCurrent = aggregatedCurrent;
      this.resultFolder = resultFolder;
      this.iterations = iterations;
      this.call = call;
   }

   public void write() throws IOException {
      writeHistogram(aggregatedPredecessor, new File(resultFolder, call + "_predecessor_" + iterations + ".csv"));
      writeHistogram(aggregatedCurrent, new File(resultFolder, call + "_current_" + iterations + ".csv"));
   }

   public void writeHistogram(final double[] aggregatedPredecessor, final File predecessorFile) throws IOException {
      try (BufferedWriter histogramWriter = new BufferedWriter(new FileWriter(predecessorFile))) {
         for (double d : aggregatedPredecessor) {
            histogramWriter.write(d + "\n");
         }
         histogramWriter.flush();
      }
   }
}
