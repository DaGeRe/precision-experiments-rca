package de.peass.validate_rca;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestCreator {

   private final boolean createBytecodeweavingEnvironment;
   private final int iterations, repetitions;
   private final int childCount;
   
   public TestCreator(final boolean createBytecodeweavingEnvironment, final int iterations, final int repetitions, final int childCount) {
      this.createBytecodeweavingEnvironment = createBytecodeweavingEnvironment;
      this.iterations = iterations;
      this.repetitions = repetitions;
      this.childCount = childCount;
   }

   public void createTest(final File clazzFolder) throws IOException {
      final File clazz = new File(clazzFolder, "MainTest.java");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(clazz))) {
         writer.write("package de.peass;\n\n");

         if (createBytecodeweavingEnvironment) {
            writer.write("import de.dagere.kopeme.annotations.PerformanceTest;\n");
            writer.write("import de.dagere.kopeme.junit.testrunner.PerformanceTestRunnerJUnit;\n");
            writer.write("import org.junit.runner.RunWith;\n");
         }

         writer.write("import org.junit.Test;\n\n");

         if (createBytecodeweavingEnvironment) {
            writer.write("@RunWith(PerformanceTestRunnerJUnit.class)\n");
         }
         writer.write("public class MainTest{ \n");

         if (createBytecodeweavingEnvironment) {
            writer.write("  @PerformanceTest(warmup=" + iterations + ""
                  + ", iterations=" + iterations + ", "
                  + "repetitions=" + repetitions + ", "
                  + "dataCollectors = \"ONLYTIME\", "
                  + "timeout=3600000, "
                  + "redirectToNull = true)\n");
         }
         writer.write("  @Test \n");
         writer.write("  public void testMe(){\n");
         writer.write("    C0_0 object = new C0_0();\n");
         for (int i = 0; i < childCount; i++) {
            writer.write("    object.method" + i + "();\n");
         }
         writer.write("  }\n");
         writer.write("}");

         writer.flush();
      }
   }
 
}
