package de.dagere.peass.validate_rca.measurement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.dagere.peass.PeassGlobalInfos;
import de.dagere.peass.config.MeasurementConfiguration;
import de.dagere.peass.config.StatisticsConfigurationMixin;
import de.dagere.peass.dependency.CauseSearchFolders;
import de.dagere.peass.dependency.analysis.data.TestCase;
import de.dagere.peass.dependency.execution.EnvironmentVariables;
import de.dagere.peass.dependency.execution.ExecutionConfigMixin;
import de.dagere.peass.dependency.execution.MeasurementConfigurationMixin;
import de.dagere.peass.dependency.persistence.Dependencies;
import de.dagere.peass.dependency.persistence.Version;
import de.dagere.peass.dependencyprocessors.ViewNotFoundException;
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
   private StatisticsConfigurationMixin statisticConfigMixin;

   @Option(names = { "-nodeCount", "--nodeCount" }, description = "Number of nodes that should be measures", required = true)
   int nodeCount;

   @Option(names = { "-folder", "--folder" }, description = "Folder of the project that should be analyzed", required = true)
   File projectFolder;

   @Option(names = { "-dependencyfile", "--dependencyfile" }, description = "Path to the dependencyfile")
   protected File dependencyFile;

   @Option(names = { "-version", "--version" }, description = "Only version to analyze - do not use together with startversion and endversion!")
   protected String version;

   public static void main(final String[] args) {
      final RunSomeNodeMeasurement command = new RunSomeNodeMeasurement();
      final CommandLine commandLine = new CommandLine(command);
      commandLine.execute(args);
   }

   @Override
   public Void call() throws Exception {
      Dependencies dependencies = Constants.OBJECTMAPPER.readValue(dependencyFile, Dependencies.class);
      final Version versionInfo = dependencies.getVersions().get(version);
      final String predecessor = versionInfo.getPredecessor();

      CauseSearcherConfig causeSearchConfig = new CauseSearcherConfig(new TestCase("de.peass.MainTest#testMe"), causeSearchConfigMixin);

      CauseSearchFolders folders = new CauseSearchFolders(projectFolder);

      MeasurementConfiguration measurementConfiguration = new MeasurementConfiguration(measurementConfigMixin, executionMixin, statisticConfigMixin);
      measurementConfiguration.setVersion(version);
      measurementConfiguration.setVersionOld(predecessor);
      measurementConfiguration.setUseKieker(true);

      List<CallTreeNode> includedNodes = getIncludedNodes(causeSearchConfig, folders, measurementConfiguration);

      CauseTester tester = new CauseTester(folders, measurementConfiguration, causeSearchConfig, new EnvironmentVariables());

      PeassGlobalInfos.isTwoVersionRun = false;

      // tester.measureVersion(includedNodes);
      tester.setIncludedMethods(new HashSet<>(includedNodes));
      final File logFolder = folders.getLogFolder(measurementConfiguration.getVersion(), causeSearchConfig.getTestCase());
      tester.setCurrentVersion(version);
      for (int i = 0; i < measurementConfiguration.getVms(); i++) {
         tester.runOnce(causeSearchConfig.getTestCase(), predecessor, i, logFolder);
      }

      return null;
   }

   private List<CallTreeNode> getIncludedNodes(final CauseSearcherConfig causeSearchConfig, final CauseSearchFolders folders, final MeasurementConfiguration measurementConfiguration)
         throws InterruptedException, IOException, FileNotFoundException, XmlPullParserException, ViewNotFoundException, AnalysisConfigurationException, JsonGenerationException,
         JsonMappingException {
      final TreeReader resultsManager = TreeReaderFactory.createTreeReader(folders, measurementConfiguration.getVersionOld(),
            measurementConfiguration,
            causeSearchConfig.isIgnoreEOIs(), new EnvironmentVariables());
      CallTreeNode root = resultsManager.getTree(new TestCase("de.peass.MainTest#testMe"), measurementConfiguration.getVersionOld());

      File potentialCacheFileOld = new File(folders.getTreeCacheFolder(measurementConfiguration.getVersion(), causeSearchConfig.getTestCase()),
            measurementConfiguration.getVersionOld());
      Constants.OBJECTMAPPER.writeValue(potentialCacheFileOld, root);

      List<CallTreeNode> includedNodes = new LinkedList<>();

      root.setVersions(version, measurementConfiguration.getVersionOld());
      includedNodes.add(root);
      for (int i = 0; i < nodeCount; i++) {
         final CallTreeNode measurementNode = root.getChildren().get(i);
         includedNodes.add(measurementNode);
         measurementNode.setVersions(version, measurementConfiguration.getVersionOld());
      }
      return includedNodes;
   }

}
