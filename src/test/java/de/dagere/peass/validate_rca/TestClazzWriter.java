package de.dagere.peass.validate_rca;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import de.dagere.peass.dependency.changesreading.JavaParserProvider;

public class TestClazzWriter {
   
   private static final String EXAMPLE_CLASS_NAME = "MyExampleClass";
   private static final File clazzFolder = new File("target/current");
   
   @BeforeEach
   public void init() {
      if (!clazzFolder.exists()) {
         clazzFolder.mkdirs();
      }
   }

   @Test
   public void testBasicClazzWriter() throws IOException {
      ClazzWriter writer = new ClazzWriter(new SlowerNodeInfos(2, 1, 0), 1, "ADD", false);
      writer.createClass(2, clazzFolder, EXAMPLE_CLASS_NAME, 0, 0, new int[] {5, 6});
      
      CompilationUnit unit = JavaParserProvider.parse(new File(clazzFolder, "MyExampleClass.java"));
      Assert.assertNotNull(unit);
      
      List<ClassOrInterfaceDeclaration> clazzes = unit.findAll(ClassOrInterfaceDeclaration.class);
      ClassOrInterfaceDeclaration exampleClazz = clazzes.get(0);
      Assert.assertEquals(EXAMPLE_CLASS_NAME, exampleClazz.getNameAsString());
   }
   
   @Test
   public void testWorkloadCallClazzWriter() throws IOException {
      ClazzWriter writer = new ClazzWriter(new SlowerNodeInfos(2, 1, 0), 1, "ADD", false);
      writer.createClass(2, clazzFolder, EXAMPLE_CLASS_NAME, 0, 1, new int[] {5, 6});
      
      CompilationUnit unit = JavaParserProvider.parse(new File(clazzFolder, "MyExampleClass.java"));
      Assert.assertNotNull(unit);
      
      List<ClassOrInterfaceDeclaration> clazzes = unit.findAll(ClassOrInterfaceDeclaration.class);
      ClassOrInterfaceDeclaration exampleClazz = clazzes.get(0);
      Assert.assertEquals(EXAMPLE_CLASS_NAME, exampleClazz.getNameAsString());
      
      MatcherAssert.assertThat(unit.toString(), Matchers.containsString("rm.addSomething(5)"));
   }
}
