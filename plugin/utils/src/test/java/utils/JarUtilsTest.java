package utils;

import org.junit.jupiter.api.Test;
import utils.files.JarUtils;

import java.util.jar.JarFile;

public class JarUtilsTest extends AbstractTest {

  @Test
  void shouldGetEntryFromJar() throws Exception {
    var jarFile = new JarFile(getTestResource("test.jar").toFile());

    var result = JarUtils.getContentFromJar(jarFile, "messages");
    assertEquals(2, result.size());

    jarFile.close();
  }

}
