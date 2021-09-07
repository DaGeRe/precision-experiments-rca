package de.dagere.peass.validate_rca;

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
   private String type = "ADD";

   @Option(names = { "-leafsHaveWorkloads", "--leafsHaveWorkloads" }, description = "Whether leafs have workloads", required = false)
   private boolean leafsHaveWorkloads = false;

   @Option(names = { "-createBytecodeweavingEnvironment",
         "--createBytecodeweavingEnvironment" }, description = "Whether to make the project bytecode-weaving ready", required = false)
   private boolean createBytecodeweavingEnvironment = false;

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

      if (!createBytecodeweavingEnvironment) {
         GenerateProject.initEmptyProject(out, "pom.xml");
      } else {
         GenerateProject.initEmptyProject(out, "bytecodeWeaving/pom.xml");
         URL pomResource = GenerateProject.class.getClassLoader().getResource("bytecodeWeaving/kieker.monitoring.properties");
         File metaInfFolder = new File(out, "src/main/resources/META-INF");
         metaInfFolder.mkdirs();
         FileUtils.copyURLToFile(pomResource, new File(metaInfFolder, "kieker.monitoring.properties"));
      }

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

   public void createFastVersion(final File projectFolder) throws IOException, InterruptedException {
      generateClasses(projectFolder, -1);

      GenerateProject.init(projectFolder);
      GenerateProject.createVersion(projectFolder, "Fast Version");
      System.out.println("Fast version created");
   }

   private void createSlowVersion(final File projectFolder) throws IOException, InterruptedException {
      FileUtils.deleteQuietly(new File(projectFolder, "src/main/java"));

      generateClasses(projectFolder, nodeInfos.getSlowerLevel());
      GenerateProject.createVersion(projectFolder, "Slow Version");
      System.out.println("Slow version created");
   }

   private void generateClasses(final File projectFolder, final int slowLevel) throws IOException {
      final File clazzFolder = new File(projectFolder, "src/main/java/de/dagere/peass");
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

      final File testFolder = new File(projectFolder, "src/test/java/de/dagere/peass");
      testFolder.mkdirs();
      new TestCreator(createBytecodeweavingEnvironment, 5, 100000, childCount).createTest(testFolder);
   }

   private void writeWorkladClasses(final File clazzFolder) throws IOException {
      if (type.equals("ADD") || type.equals("ADDITION")) {
         URL addResource = GenerateProject.class.getClassLoader().getResource("workloads/AddRandomNumbers.java");
         FileUtils.copyURLToFile(addResource, new File(clazzFolder, "AddRandomNumbers.java"));
      } else if (type.equals("RAM") || type.equals("RESERVE_RAM")) {
         URL reserveRAM = GenerateProject.class.getClassLoader().getResource("workloads/ReserveRAM.java");
         FileUtils.copyURLToFile(reserveRAM, new File(clazzFolder, "ReserveRAM.java"));
      } else if (type.equals("SYSOUT") || type.equals("WRITE_TO_SYSOUT")) {
         URL writeSysout = GenerateProject.class.getClassLoader().getResource("workloads/WriteToSystemOut.java");
         FileUtils.copyURLToFile(writeSysout, new File(clazzFolder, "WriteToSystemOut.java"));
      } else if (type.equals("THROW")) {
         final File trowSomething = new File("src/main/resources/workloads/ThrowSomething.java");
         FileUtils.copyFile(trowSomething, new File(clazzFolder, "ThrowSomething.java"));
      } else {
         throw new RuntimeException("Unknown workload type: " + type);
      }
   }

   private void createClass(final int methods, final File clazzFolder, final String className, final int classIndex, final int treeLevel, final int[] durations)
         throws IOException {
      final File clazz = new File(clazzFolder, className + ".java");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(clazz))) {
         writer.write("package de.dagere.peass;\n\n");

         writer.write("class " + className + "{ \n");
         for (int method = 0; method < methods; method++) {
            writer.write(" public int method" + method + "(){\n");
            if (treeLevel < nodeInfos.getTreeDepth() - 1) {
               String name = "  C" + (treeLevel + 1) + "_" + (2 * classIndex + method);
               writer.write(name + " object = new " + name + "();\n");
               writer.write("  int value = 0;\n");
               for (int i = 0; i < childCount; i++) {
                  writer.write("  value += object.method" + i + "();\n");
               }
               writer.write("  return value;");
            } else {
               writeWorkload(writer, durations[method]);
            }

            writer.write(" }\n");
         }
         writer.write("}");

         writer.flush();
      }
   }

   private void writeWorkload(final BufferedWriter writer, final int parameter) throws IOException {
      if (parameter > 0) {
         if (type.equals("BUSY_WAITING")) {
            writer.write("         final long exitTime = System.nanoTime() + " + parameter + ";\n" +
                  "         long currentTime;\n" +
                  "         do {\n" +
                  "            currentTime = System.nanoTime();\n" +
                  "         } while (currentTime < exitTime);" +
                  "         return (int)exitTime;\n");
         } else if (type.equals("ADD") || type.equals("ADDITION")) {
            writer.write("         final AddRandomNumbers rm = new AddRandomNumbers();\n" +
                  "            return rm.addSomething(" + parameter + ");\n");

         } else if (type.equals("RAM") || type.equals("RESERVE_RAM")) {
            writer.write("         final ReserveRAM rm = new ReserveRAM();\n" +
                  "            return rm.doSomething(" + parameter + ");\n");
         } else if (type.equals("SYSOUT") || type.equals("WRITE_TO_SYSOUT")) {
            writer.write("         final WriteToSystemOut rm = new WriteToSystemOut();\n" +
                  "            return rm.doSomething(" + parameter + ");\n");
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
      } else {
         writer.write("return 0;");
      }
   }

   public SlowerNodeInfos getNodeInfos() {
      return nodeInfos;
   }

   public void setNodeInfos(final SlowerNodeInfos nodeInfos) {
      this.nodeInfos = nodeInfos;
   }

   public String getType() {
      return type;
   }

   public void setType(final String type) {
      this.type = type;
   }

   public int getChildCount() {
      return childCount;
   }

   public void setChildCount(final int childCount) {
      this.childCount = childCount;
   }

   public File getOut() {
      return out;
   }

   public void setOut(final File out) {
      this.out = out;
   }
}
