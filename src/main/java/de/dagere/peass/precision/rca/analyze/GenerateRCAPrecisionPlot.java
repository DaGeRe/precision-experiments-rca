package de.dagere.peass.precision.rca.analyze;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dagere.peass.config.StatisticsConfig;
import de.dagere.peass.folders.CauseSearchFolders;
import de.dagere.peass.measurement.rca.data.CauseSearchData;
import de.dagere.peass.measurement.rca.serialization.MeasuredNode;
import de.dagere.peass.measurement.statistics.Relation;
import de.dagere.peass.utils.Constants;
import de.precision.analysis.repetitions.PrecisionConfig;
import de.precision.analysis.repetitions.PrecisionConfigMixin;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

public class GenerateRCAPrecisionPlot implements Callable<Void> {

   private static final Logger LOG = LogManager.getLogger(GenerateRCAPrecisionPlot.class);

   @Option(names = { "-type1error", "--type1error" }, description = "Type 1 error of executed statistic tests")
   private double type1error = 0.01;

   @Option(names = { "-data", "--data" }, description = "Data-Folder for analysis", required = true)
   private String[] data;

   @Option(names = { "-alsoPlotChilds", "--alsoPlotChilds" }, description = "Plot childs", required = false)
   private boolean alsoPlotChilds = false;
   
   @Mixin
   private PrecisionConfigMixin precisionConfigMixin;

   private StatisticsConfig config;

   public static void main(final String[] args) {

      GenerateRCAPrecisionPlot command = new GenerateRCAPrecisionPlot();
      final CommandLine commandLine = new CommandLine(command);
      commandLine.execute(args);
   }

   @Override
   public Void call() throws Exception {

      PrecisionConfig precisionConfig = precisionConfigMixin.getConfig();
      
      config = new StatisticsConfig();
      if (precisionConfig.isRemoveOutliers()) {
         config.setOutlierFactor(StatisticsConfig.DEFAULT_OUTLIER_FACTOR);
      } else {
         config.setOutlierFactor(0.0);
      }
      config.setType1error(type1error);
      
      for (String folder : data) {
         final File basicFolder = new File(folder);
         CauseSearchFolders folders = new CauseSearchFolders(basicFolder);
         System.out.println(folders.getRcaTreeFolder().getAbsolutePath());
         File versionFolder = folders.getRcaTreeFolder().listFiles()[0];
         File testclazzFolder = versionFolder.listFiles()[0];
         File detailsFolder = new File(testclazzFolder, "details");
         File detailsFile = new File(detailsFolder, "testMe.json");
         CauseSearchData data = Constants.OBJECTMAPPER.readValue(detailsFile, CauseSearchData.class);

         File resultFolder = new File(basicFolder.getParentFile(), precisionConfig.isRemoveOutliers() ? "results_outlierRemoval" : "results_noOutlierRemoval");
         resultFolder.mkdirs();
         
         final MeasuredNode rootNode = data.getNodes();
         plotNode(rootNode, resultFolder, precisionConfig);
      }

      return null;
   }

   private void plotNode(final MeasuredNode rootNode, final File resultFolder, final PrecisionConfig precisionConfig) {
      LOG.info("Checking LESS_THAN: {}", rootNode.getCall());
      new NodePrecisionPlotGenerator(rootNode, Relation.LESS_THAN, config, 1000, precisionConfig).generate(resultFolder);

      if (alsoPlotChilds) {
         for (MeasuredNode child : rootNode.getChildren()) {
            if (child.getCall().endsWith("method0")) {
               plotNode(child, resultFolder, precisionConfig);
            } else {
               LOG.info("Checking EQUAL: {}", child.getCall());
               new NodePrecisionPlotGenerator(child, Relation.EQUAL, config, 10000, precisionConfig).generate(resultFolder);
            }
         }
      }
   }

}
