package utils.files;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Utility class to perform actions on a JAR
 */

@UtilityClass
public class JarUtils {

  @SneakyThrows
  public File getJar() {
    return new File(JarUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
  }

  public List<JarEntry> getContentFromJar(JarFile jarFile, String path) {
    return Collections.list(jarFile.entries()).stream()
        .filter(entry -> entry.getName().startsWith(path))
        .filter(entry -> !entry.isDirectory())
        .collect(Collectors.toList());
  }

}
