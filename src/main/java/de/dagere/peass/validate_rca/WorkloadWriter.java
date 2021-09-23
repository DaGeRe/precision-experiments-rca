package de.dagere.peass.validate_rca;

import java.io.IOException;
import java.io.Writer;

public class WorkloadWriter {

   private static final String SPACES = "         ";
   private final String type;
   final boolean addSpace;

   public WorkloadWriter(final String type, final boolean addSpace) {
      this.type = type;
      this.addSpace = addSpace;
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
      writer.write(SPACES + "final WriteToSystemOut rm = new WriteToSystemOut();\n");
      writeDoSomething(writer, parameter);
   }

   private void writeRamWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write(SPACES + "final ReserveRAM rm = new ReserveRAM();\n");
      writeDoSomething(writer, parameter);
   }

   private void writeAddWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write(SPACES + "final AddRandomNumbers rm = new AddRandomNumbers();\n");
      writeDoSomething(writer, parameter);
   }

   private void writeDoSomething(final Writer writer, final int parameter) throws IOException {
      if (addSpace) {
         writer.write(SPACES + "final int result = rm.doSomething(" + parameter + ");\n");
         writer.write(SPACES + "return result;\n");
      } else {
         writer.write(SPACES + "return rm.doSomething(" + parameter + ");\n");
      }
   }

   private void writeBusyWaitingWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write(SPACES + "final long exitTime = System.nanoTime() + " + parameter + ";\n" +
            SPACES + "long currentTime;\n" +
            SPACES + "do {\n" +
            SPACES + "  currentTime = System.nanoTime();\n" +
            SPACES + "} while (currentTime < exitTime);");
      if (addSpace) {
         writer.write(SPACES + "final int result = (int)exitTime");
         writer.write(SPACES + "return result;");
      } else {
         writer.write(SPACES + "return (int)exitTime;\n");
      }

   }

   private void writeThrowWorkload(final Writer writer, final int parameter) throws IOException {
      writer.write(SPACES + "for (int i = 0; i < " + parameter + "; i++) {\n" +
            SPACES + "  try {\n" +
            SPACES + "    ThrowSomething doSomething = new ThrowSomething();\n");
      if (addSpace) {
         writer.write(SPACES + "    int result = doSomething.returnMe(1);\n");
      } else {
         writer.write(SPACES + "    doSomething.returnMe(1);\n");
      }
      writer.write(SPACES + "  } catch (RuntimeException e) {\n" +
            SPACES + "  }\n" +
            SPACES +"}");
   }
}
