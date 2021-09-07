package de.dagere.peass.validate_rca;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestTreeGeneration {
   
   @Rule
   public TemporaryFolder rule = new TemporaryFolder();
   
   @Test
   public void testProjectCreation() throws Exception {
      File root = rule.getRoot();
      
      final GenerateTreeExampleProject generator = new GenerateTreeExampleProject();
      generator.setOut(root);
      generator.setChildCount(2);
      generator.setNodeInfos(new SlowerNodeInfos(5, 4, 0));
      generator.call();
      
      final File exampleFile = new File(root, "src/main/java/de/dagere/peass/C3_7.java");
      String text = new String(Files.readAllBytes(exampleFile.toPath()), StandardCharsets.UTF_8);

      MatcherAssert.assertThat(text, Matchers.containsString("C4_14"));
      MatcherAssert.assertThat(text, Matchers.containsString("C4_15"));
   }
}
