package de.peass.validate_rca.analyze;

import java.io.File;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.peass.config.MeasurementConfiguration;
import de.peass.measurement.rca.data.CauseSearchData;
import de.peass.measurement.rca.serialization.MeasuredNode;

class VMAnalyzer extends FolderAnalyzer {
   public VMAnalyzer() {
      super(false);
   }

   private DescriptiveStatistics statisticsDeviation = new DescriptiveStatistics();
   private DescriptiveStatistics statisticsDeviationRelative = new DescriptiveStatistics();

   public void processNode(File durationFolder, MeasuredNode node, CauseSearchData data) {
      System.out.println(durationFolder.getName() + " " + (!node.getChildren().isEmpty()));
      this.config = data.getMeasurementConfig();

      VMAnalyzer analyzerLocal = new VMAnalyzer();
      analyzerLocal.addNodeData(node);
      this.addNodeData(node);
      analyzerLocal.print();
      addNodeData(node);
   }

   public void print() {
      System.out.println("Absolute: " + statisticsDeviation.getMean() + " " + statisticsDeviation.getMax());
      System.out.println("Relative: " + statisticsDeviationRelative.getMean() + " " + statisticsDeviationRelative.getMax());
   }

   public void addNodeData(MeasuredNode node) {
      // System.out.println(node.getKiekerPattern());
      addData(node);

      for (MeasuredNode child : node.getChildren()) {
         addNodeData(child);
      }
   }

   private void addData(MeasuredNode node) {
      if (node.getStatistic().getMeanCurrent() != 0) {
         statisticsDeviation.addValue(node.getStatistic().getDeviationCurrent());
         statisticsDeviationRelative.addValue(node.getStatistic().getDeviationCurrent() / node.getStatistic().getMeanCurrent());
      }
      if (node.getStatistic().getMeanOld() != 0) {
         statisticsDeviation.addValue(node.getStatistic().getDeviationOld());
         statisticsDeviationRelative.addValue(node.getStatistic().getDeviationOld() / node.getStatistic().getMeanOld());
      }
   }
}