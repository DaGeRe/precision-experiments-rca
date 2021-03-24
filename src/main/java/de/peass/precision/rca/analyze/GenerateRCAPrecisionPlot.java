package de.peass.precision.rca.analyze;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.peass.dependency.CauseSearchFolders;
import de.peass.measurement.analysis.Relation;
import de.peass.measurement.rca.data.CauseSearchData;
import de.peass.measurement.rca.serialization.MeasuredNode;
import de.peass.utils.Constants;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class GenerateRCAPrecisionPlot implements Callable<Void> {

   private static final Logger LOG = LogManager.getLogger(GenerateRCAPrecisionPlot.class);

   @Option(names = { "-removeOutliers", "--removeOutliers" }, description = "Whether to remove outliers")
   private boolean removeOutliers;

   @Option(names = { "-data", "--data" }, description = "Data-Folder for analysis", required = true)
   private String[] data;

   @Option(names = { "-alsoPlotChilds", "--alsoPlotChilds" }, description = "Plot childs", required = false)
   private boolean alsoPlotChilds = false;

   public static void main(final String[] args) {

      GenerateRCAPrecisionPlot command = new GenerateRCAPrecisionPlot();
      final CommandLine commandLine = new CommandLine(command);
      commandLine.execute(args);
   }

   @Override
   public Void call() throws Exception {
      for (String folder : data) {
         final File basicFolder = new File(folder);
         CauseSearchFolders folders = new CauseSearchFolders(basicFolder);
         System.out.println(folders.getRcaTreeFolder().getAbsolutePath());
         File versionFolder = folders.getRcaTreeFolder().listFiles()[0];
         File testclazzFolder = versionFolder.listFiles()[0];
         File detailsFolder = new File(testclazzFolder, "details");
         File detailsFile = new File(detailsFolder, "testMe.json");
         CauseSearchData data = Constants.OBJECTMAPPER.readValue(detailsFile, CauseSearchData.class);

         File resultFolder = new File(basicFolder.getParentFile(), removeOutliers ? "results_outlierRemoval" : "results_noOutlierRemoval");
         resultFolder.mkdirs();

         final MeasuredNode rootNode = data.getNodes();
         plotNode(rootNode, resultFolder);
      }

      return null;
   }

   private void plotNode(final MeasuredNode rootNode, final File resultFolder) {
      LOG.info("Checking LESS_THAN: {}", rootNode.getCall());
      new NodePrecisionPlotGenerator(rootNode, Relation.LESS_THAN, removeOutliers).generate(resultFolder);

      if (alsoPlotChilds) {
         for (MeasuredNode child : rootNode.getChildren()) {
            if (child.getCall().endsWith("method0")) {
               plotNode(child, resultFolder);
            } else {
               LOG.info("Checking EQUAL: {}", child.getCall());
               new NodePrecisionPlotGenerator(child, Relation.EQUAL, removeOutliers).generate(resultFolder);
            }
         }
      }
   }

}
