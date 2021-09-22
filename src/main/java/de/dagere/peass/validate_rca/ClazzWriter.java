package de.dagere.peass.validate_rca;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ClazzWriter {
   
   private final SlowerNodeInfos nodeInfos;
   private final int childCount;
   private final String type;
   
   public ClazzWriter(final SlowerNodeInfos nodeInfos, final int childCount, final String type) {
      this.nodeInfos = nodeInfos;
      this.childCount = childCount;
      this.type = type;
   }

   public void createClass(final int methods, final File clazzFolder, final String className, final int classIndex, final int treeLevel, final int[] durations)
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
}
