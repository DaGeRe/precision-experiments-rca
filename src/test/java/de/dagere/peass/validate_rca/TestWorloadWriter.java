package de.dagere.peass.validate_rca;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class TestWorloadWriter {

   @Test
   public void testWriterRegular() throws IOException {
      WorkloadWriter writer = new WorkloadWriter("ADD", false);
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      OutputStreamWriter outputStream = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
      writer.writeWorkload(outputStream, 300);

      outputStream.flush();

      String result = new String(stream.toByteArray());
      MatcherAssert.assertThat(result, Matchers.containsString("rm.doSomething(300)"));
   }
   
   @Test
   public void testWriterWithSpace() throws IOException {
      WorkloadWriter writer = new WorkloadWriter("ADD", true);
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      OutputStreamWriter outputStream = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
      writer.writeWorkload(outputStream, 300);

      outputStream.flush();

      String result = new String(stream.toByteArray());
      MatcherAssert.assertThat(result, Matchers.containsString("final int result ="));
   }
}
