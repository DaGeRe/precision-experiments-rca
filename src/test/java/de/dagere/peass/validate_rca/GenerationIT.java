package de.dagere.peass.validate_rca;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.dagere.peass.validate_rca.GenerateTreeExampleProject;
import de.dagere.peass.validate_rca.SlowerNodeInfos;
import de.pmdcheck.peassgeneration.GenerateProject;

public class GenerationIT {
   
   @Test
   public void testTreeGeneration() throws IOException, InterruptedException {
      File projectFolder = new File("target/temp");
      FileUtils.deleteDirectory(projectFolder);
      
      GenerateProject.initEmptyProject(projectFolder, "pom.xml");
      GenerateTreeExampleProject generator = new GenerateTreeExampleProject();
      generator.setNodeInfos(new SlowerNodeInfos(1, 1, 1));
      generator.setType("RAM");
      generator.createFastVersion(projectFolder);
      
      ProcessBuilder pb = new ProcessBuilder("mvn", "test");
      pb.directory(projectFolder);
      int returnCode = pb.start().waitFor();
      Assert.assertEquals(0, returnCode);
   }
}
