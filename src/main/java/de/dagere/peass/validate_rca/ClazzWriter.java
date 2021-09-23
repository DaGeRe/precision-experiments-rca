package de.dagere.peass.validate_rca;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ClazzWriter {
   
   private final SlowerNodeInfos nodeInfos;
   private final int childCount;
   private WorkloadWriter workloadWriter;
   
   public ClazzWriter(final SlowerNodeInfos nodeInfos, final int childCount, final String type, final boolean addSpace) {
      this.nodeInfos = nodeInfos;
      this.childCount = childCount;
      workloadWriter = new WorkloadWriter(type, addSpace);
   }

   public void createClass(final File clazzFolder, final String className, final int classIndex, final int treeLevel, final int[] durations)
         throws IOException {
      final File clazz = new File(clazzFolder, className + ".java");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(clazz))) {
         writer.write("package de.dagere.peass;\n\n");

         writer.write("class " + className + "{ \n");
         for (int method = 0; method < childCount; method++) {
            writer.write(" public int method" + method + "(){\n");
            if (treeLevel < nodeInfos.getTreeDepth() - 1) {
               writeSubclassCall(classIndex, treeLevel, writer, method);
            } else {
               workloadWriter.writeWorkload(writer, durations[method]);
            }

            writer.write(" }\n");
         }
         writer.write("}");

         writer.flush();
      }
   }

   private void writeSubclassCall(final int classIndex, final int treeLevel, final BufferedWriter writer, final int method) throws IOException {
      String name = "  C" + (treeLevel + 1) + "_" + (2 * classIndex + method);
      writer.write(name + " object = new " + name + "();\n");
      writer.write("  int value = 0;\n");
      for (int i = 0; i < childCount; i++) {
         writer.write("  value += object.method" + i + "();\n");
      }
      writer.write("  return value;");
   }
   
}
