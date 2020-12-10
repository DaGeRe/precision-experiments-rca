package de.peass.validate_rca;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import de.pmdcheck.peassgeneration.GenerateProject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(description = "Generate example rca project with one slowed down method in the tree. The workload of one tree node differs, it has a fast and a slow variant, which is defined by the parameter.", name = "generateExample")
public class GenerateTreeExampleProject implements Callable<Integer> {

   @Mixin
   private SlowerNodeInfos nodeInfos;

   @Option(names = { "-fastParameter", "--fastParameter" }, description = "Parameter of fast methods", required = false)
   private int fastParameter = 10000;

   @Option(names = { "-slowParameter", "--slowParameter" }, description = "Parameter of slow methods", required = false)
   private int slowParameter = 10100;

   @Option(names = { "-childCount", "--childCount" }, description = "Number of childs per node", required = false)
   private int childCount = 2;

   @Option(names = { "-type", "--type" }, description = "Type of slowed down workload", required = false)
   private String type = "BUSY_WAITING";

   @Option(names = { "-leafsHaveWorkloads", "--leafsHaveWorkloads" }, description = "Whether leafs have workloads", required = false)
   private boolean leafsHaveWorkloads = false;

   @Option(names = { "-out", "--out" }, description = "Result folder for the project; defaults to target/project_$X where $X is the tree depth", required = false)
   private File out = null;

   public static void main(final String[] args) {
      final GenerateTreeExampleProject command = new GenerateTreeExampleProject();
      final CommandLine commandLine = new CommandLine(command);
      commandLine.execute(args);
   }

   @Override
   public Integer call() throws Exception {
      checkParameters();

      if (out == null) {
         out = new File("target/project_" + nodeInfos.getTreeDepth());
      }

      GenerateProject.initEmptyProject(out);
      out.mkdir();

      createFastVersion(out);

      createSlowVersion(out);
      return null;

   }

   private void checkParameters() {
      if (slowParameter < fastParameter) {
         throw new RuntimeException("Slow parameter " + slowParameter + " needs to be greater than fast parameter " + fastParameter);
      }
      if (nodeInfos.getSlowerLevel() >= nodeInfos.getTreeDepth()) {
         throw new RuntimeException("Tree depth " + nodeInfos.getTreeDepth() + " needs to be smaller than slower level " + nodeInfos.getSlowerLevel());
      }
   }

   private void createFastVersion(final File projectFolder) throws IOException, InterruptedException {
      generateClasses(projectFolder, -1);

      GenerateProject.init(projectFolder);
      GenerateProject.createVersion(projectFolder, "Fast Version");
      System.out.println("Fast version created");
   }

   private void createSlowVersion(final File projectFolder) throws IOException, InterruptedException {
      FileUtils.deleteQuietly(new File(projectFolder, "src"));

      generateClasses(projectFolder, nodeInfos.getSlowerLevel());
      GenerateProject.createVersion(projectFolder, "Slow Version");
      System.out.println("Slow version created");
   }

   private void generateClasses(final File projectFolder, int slowLevel) throws IOException {
      final File clazzFolder = new File(projectFolder, "src/main/java/de/peass");
      clazzFolder.mkdirs();
      writeWorkladClasses(clazzFolder);

      DurationCreator creator = new DurationCreator(fastParameter, slowParameter, leafsHaveWorkloads, childCount, nodeInfos);

      for (int treeLevel = 0; treeLevel < nodeInfos.getTreeDepth(); treeLevel++) {
         final double levelClassCount = Math.pow(childCount, treeLevel);
         for (int classIndex = 0; classIndex < levelClassCount; classIndex++) {
            // System.out.println(treeLevel + " " + classIndex);
            final String className = "C" + treeLevel + "_" + classIndex;

            final int[] durations = creator.getDuration(treeLevel, classIndex, slowLevel);

            createClass(childCount, clazzFolder, className, classIndex, treeLevel, durations);
         }
      }

      final File testFolder = new File(projectFolder, "src/test/java/de/peass");
      testFolder.mkdirs();
      createTest(testFolder);
   }

   private void writeWorkladClasses(final File clazzFolder) throws IOException {
      if (type.equals("ADD") || type.equals("ADDITION")) {
         URL addResource = GenerateProject.class.getClassLoader().getResource("workloads/AddRandomNumbers.java");
         FileUtils.copyURLToFile(addResource, new File(clazzFolder, "AddRandomNumbers.java"));
      } else if (type.equals("RAM") || type.equals("RESERVE_RAM")) {
         final File reserveRAM = new File("src/main/resources/workloads/ReserveRAM.java");
         FileUtils.copyFile(reserveRAM, new File(clazzFolder, "ReserveRAM.java"));
      } else if (type.equals("SYSOUT") || type.equals("WRITE_TO_SYSOUT")) {
         final File writeSysout = new File("src/main/resources/workloads/WriteToSystemOut.java");
         FileUtils.copyFile(writeSysout, new File(clazzFolder, "WriteToSystemOut.java"));
      } else if (type.equals("THROW")) {
         final File trowSomething = new File("src/main/resources/workloads/ThrowSomething.java");
         FileUtils.copyFile(trowSomething, new File(clazzFolder, "ThrowSomething.java"));
      }
   }

   private void createTest(final File clazzFolder) throws IOException {
      final File clazz = new File(clazzFolder, "MainTest.java");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(clazz))) {
         writer.write("package de.peass;\n\n");

         writer.write("import org.junit.Test;\n\n");

         writer.write("public class MainTest{ \n");
         writer.write(" @Test \n");
         writer.write(" public void testMe(){\n");
         writer.write("  C0_0 object = new C0_0();\n");
         for (int i = 0; i < childCount; i++) {
            writer.write("  object.method" + i + "();\n");
         }
         writer.write(" }\n");
         writer.write("}");

         writer.flush();
      }
   }

   private void createClass(final int methods, final File clazzFolder, final String className, final int classIndex, final int treeLevel, int[] durations) throws IOException {
      final File clazz = new File(clazzFolder, className + ".java");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(clazz))) {
         writer.write("package de.peass;\n\n");

         writer.write("class " + className + "{ \n");
         for (int method = 0; method < methods; method++) {
            writer.write(" public void method" + method + "(){\n");
            if (treeLevel < nodeInfos.getTreeDepth() - 1) {
               String name = "  C" + (treeLevel + 1) + "_" + (2 * classIndex + method);
               writer.write(name + " object = new " + name + "();\n");
               for (int i = 0; i < childCount; i++) {
                  writer.write("  object.method" + i + "();\n");
               }
            }
            writeWorkload(writer, durations[method]);
            writer.write(" }\n");
         }
         writer.write("}");

         writer.flush();
      }
   }

   private void writeWorkload(BufferedWriter writer, int parameter) throws IOException {
      if (parameter > 0) {
         if (type.equals("BUSY_WAITING")) {
            writer.write("         final long exitTime = System.nanoTime() + " + parameter + ";\n" +
                  "         long currentTime;\n" +
                  "         do {\n" +
                  "            currentTime = System.nanoTime();\n" +
                  "         } while (currentTime < exitTime);\n");
         } else if (type.equals("ADD") || type.equals("ADDITION")) {
            writer.write("         final AddRandomNumbers rm = new AddRandomNumbers();\n" +
                  "         for (int i = 0; i < " + parameter + "; i++) {\n" +
                  "            rm.addSomething();\n" +
                  "         }");

         } else if (type.equals("RAM") || type.equals("RESERVE_RAM")) {
            writer.write("         final ReserveRAM rm = new ReserveRAM(" + parameter + ");\n" +
                  "         for (int i = 0; i < 100; i++) {\n" +
                  "            rm.doSomething();\n" +
                  "         }");
         } else if (type.equals("SYSOUT") || type.equals("WRITE_TO_SYSOUT")) {
            writer.write("         final WriteToSystemOut rm = new WriteToSystemOut();\n" +
                  "         for (int i = 0; i < " + parameter + "; i++) {\n" +
                  "            rm.doSomething();\n" +
                  "         }");
         } else if (type.equals("THROW")) {
            writer.write("         final AddRandomNumbers rm = new AddRandomNumbers();\n" +
                  "         for (int i = 0; i < " + parameter + "; i++) {\n" +
                  "            try {\n" +
                  "         ThrowSomething doSomething = new ThrowSomething();\n" +
                  "         doSomething.returnMe(1);\n" +
                  "      } catch (RuntimeException e) {\n" +
                  "      }\n" +
                  "         }");
         } else {
            throw new RuntimeException("Unexpected type: " + type);
         }
      }
   }

   public SlowerNodeInfos getNodeInfos() {
      return nodeInfos;
   }

   public void setNodeInfos(SlowerNodeInfos nodeInfos) {
      this.nodeInfos = nodeInfos;
   }

   public int getChildCount() {
      return childCount;
   }

   public void setChildCount(int childCount) {
      this.childCount = childCount;
   }

   public File getOut() {
      return out;
   }

   public void setOut(File out) {
      this.out = out;
   }
}
