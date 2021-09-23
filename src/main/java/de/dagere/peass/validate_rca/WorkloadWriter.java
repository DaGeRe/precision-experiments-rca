package de.dagere.peass.validate_rca;

import java.io.IOException;
import java.io.Writer;

public class WorkloadWriter {

   private final String type;
   final boolean addSpace;

   public WorkloadWriter(final String type, final boolean addSpace) {
      this.type = type;
      this.addSpace = addSpace;
   }

   private String getParameterString(final int parameter) {
      if (addSpace) {
         return parameter + " ";
      } else {
         return parameter + "";
      }
   }

   public void writeWorkload(final Writer writer, final int parameter) throws IOException {
      if (parameter > 0) {
         if (type.equals("BUSY_WAITING")) {
            writeBusyWaitingWorkload(writer, parameter);
         } else if (type.equals("ADD") || type.equals("ADDITION")) {
            writeAddWorkload(writer, parameter);
         } else if (type.equals("RAM") || type.equals("RESERVE_RAM")) {
            writeRamWorkload(writer, parameter);
         } else if (type.equals("SYSOUT") || type.equals("WRITE_TO_SYSOUT")) {
            writeSysoutWorkload(writer, parameter);
         } else if (type.equals("THROW")) {
            writeThrowWorkload(writer, parameter);
         } else {
            throw new RuntimeException("Unexpected type: " + type);
         }
      } else {
         writer.write("return 0;");
      }
   }

   private void writeSysoutWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write("         final WriteToSystemOut rm = new WriteToSystemOut();\n" +
            "            return rm.doSomething(" + getParameterString(parameter) + ");\n");
   }

   private void writeRamWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write("         final ReserveRAM rm = new ReserveRAM();\n" +
            "            return rm.doSomething(" + getParameterString(parameter) + ");\n");
   }

   private void writeAddWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write("         final AddRandomNumbers rm = new AddRandomNumbers();\n" +
            "            return rm.addSomething(" + getParameterString(parameter) + ");\n");
   }

   private void writeBusyWaitingWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write("         final long exitTime = System.nanoTime() + " + getParameterString(parameter) + ";\n" +
            "         long currentTime;\n" +
            "         do {\n" +
            "            currentTime = System.nanoTime();\n" +
            "         } while (currentTime < exitTime);" +
            "         return (int)exitTime;\n");
   }

   private void writeThrowWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write("         final AddRandomNumbers rm = new AddRandomNumbers();\n" +
            "         for (int i = 0; i < " + getParameterString(parameter) + "; i++) {\n" +
            "            try {\n" +
            "         ThrowSomething doSomething = new ThrowSomething();\n" +
            "         doSomething.returnMe(1);\n" +
            "      } catch (RuntimeException e) {\n" +
            "      }\n" +
            "         }");
   }
}
