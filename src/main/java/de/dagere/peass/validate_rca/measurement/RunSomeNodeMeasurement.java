package de.dagere.peass.validate_rca.measurement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peass.config.MeasurementConfig;
import de.dagere.peass.config.parameters.ExecutionConfigMixin;
import de.dagere.peass.config.parameters.KiekerConfigMixin;
import de.dagere.peass.config.parameters.MeasurementConfigurationMixin;
import de.dagere.peass.config.parameters.StatisticsConfigMixin;
import de.dagere.nodeDiffDetector.data.TestMethodCall;
import de.dagere.peass.dependency.persistence.CommitStaticSelection;
import de.dagere.peass.dependency.persistence.StaticTestSelection;
import de.dagere.peass.dependencyprocessors.CommitComparatorInstance;
import de.dagere.peass.execution.utils.EnvironmentVariables;
import de.dagere.peass.folders.CauseSearchFolders;
import de.dagere.peass.measurement.rca.CauseSearcherConfig;
import de.dagere.peass.measurement.rca.CauseSearcherConfigMixin;
import de.dagere.peass.measurement.rca.CauseTester;
import de.dagere.peass.measurement.rca.data.CallTreeNode;
import de.dagere.peass.measurement.rca.kieker.TreeReader;
import de.dagere.peass.measurement.rca.kieker.TreeReaderFactory;
import de.dagere.peass.utils.Constants;
import kieker.analysis.exception.AnalysisConfigurationException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(description = "Measures the given count of nodes in an artificial validation project", name = "measuresNodes")
public class RunSomeNodeMeasurement implements Callable<Void> {

   @Mixin
   ExecutionConfigMixin executionMixin;

   @Mixin
   CauseSearcherConfigMixin causeSearchConfigMixin;

   @Mixin
   MeasurementConfigurationMixin measurementConfigMixin;

   @Mixin
   private StatisticsConfigMixin statisticConfigMixin;

   @Option(names = { "-nodeCount", "--nodeCount" }, description = "Number of nodes that should be measures", required = true)
   int nodeCount;

   @Option(names = { "-folder", "--folder" }, description = "Folder of the project that should be analyzed", required = true)
   File projectFolder;

   @Option(names = { "-staticSelectionFile", "--staticSelectionFile" }, description = "Path to the staticSelectionFile")
   protected File staticSelectionFile;

   @Option(names = { "-commit", "--commit" }, description = "Only commit to analyze - do not use together with startcommit and endcommit!")
   protected String commit;

   public static void main(final String[] args) {
      final RunSomeNodeMeasurement command = new RunSomeNodeMeasurement();
      final CommandLine commandLine = new CommandLine(command);
      commandLine.execute(args);
   }

   @Override
   public Void call() throws Exception {
      StaticTestSelection dependencies = Constants.OBJECTMAPPER.readValue(staticSelectionFile, StaticTestSelection.class);
      final CommitStaticSelection versionInfo = dependencies.getCommits().get(commit);
      final String predecessor = versionInfo.getPredecessor();

      CauseSearcherConfig causeSearchConfig = new CauseSearcherConfig(new TestMethodCall("de.peass.MainTest", "testMe"), causeSearchConfigMixin);

      CauseSearchFolders folders = new CauseSearchFolders(projectFolder);

      MeasurementConfig measurementConfiguration = new MeasurementConfig(measurementConfigMixin, executionMixin, statisticConfigMixin, new KiekerConfigMixin());
      measurementConfiguration.getFixedCommitConfig().setCommit(commit);
      measurementConfiguration.getFixedCommitConfig().setCommitOld(predecessor);
      measurementConfiguration.getKiekerConfig().setUseKieker(true);

      List<CallTreeNode> includedNodes = getIncludedNodes(causeSearchConfig, folders, measurementConfiguration);

      CommitComparatorInstance comparator = new CommitComparatorInstance(dependencies);
      CauseTester tester = new CauseTester(folders, measurementConfiguration, causeSearchConfig, new EnvironmentVariables(), comparator);

      // tester.measureVersion(includedNodes);
      tester.setIncludedMethods(new HashSet<>(includedNodes));
      final File logFolder = folders.getMeasureLogFolder(measurementConfiguration.getFixedCommitConfig().getCommit(), causeSearchConfig.getTestCase());
      tester.setCurrentVersion(commit);
      for (int i = 0; i < measurementConfiguration.getVms(); i++) {
         tester.runOnce(causeSearchConfig.getTestCase(), predecessor, i, logFolder);
      }

      return null;
   }

   private List<CallTreeNode> getIncludedNodes(final CauseSearcherConfig causeSearchConfig, final CauseSearchFolders folders, final MeasurementConfig measurementConfiguration)
         throws InterruptedException, IOException, FileNotFoundException, AnalysisConfigurationException, JsonGenerationException,
         JsonMappingException {
      final TreeReader resultsManager = TreeReaderFactory.createTreeReader(folders, measurementConfiguration.getFixedCommitConfig().getCommitOld(),
            measurementConfiguration,
            causeSearchConfig.isIgnoreEOIs(), new EnvironmentVariables());
      CallTreeNode root = resultsManager.getTree(new TestMethodCall("de.peass.MainTest", "testMe"), measurementConfiguration.getFixedCommitConfig().getCommitOld());

      File potentialCacheFileOld = new File(folders.getTreeCacheFolder(measurementConfiguration.getFixedCommitConfig().getCommit(), causeSearchConfig.getTestCase()),
            measurementConfiguration.getFixedCommitConfig().getCommitOld());
      Constants.OBJECTMAPPER.writeValue(potentialCacheFileOld, root);

      List<CallTreeNode> includedNodes = new LinkedList<>();

      root.setConfig(measurementConfiguration);

      root.initCommitData();
      includedNodes.add(root);
      for (int i = 0; i < nodeCount; i++) {
         final CallTreeNode measurementNode = root.getChildren().get(i);
         includedNodes.add(measurementNode);
         measurementNode.initCommitData();
      }
      return includedNodes;
   }

}
