package utils;

import org.junit.jupiter.api.Assertions;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractTest extends Assertions {
  public Path getTestResource(String resourceName) {
    // Obtain the class loader for the current class
    ClassLoader classLoader = getClass().getClassLoader();

    // Use the class loader to get the URL of the resource
    // This assumes that the resource is in the classpath
    // Adjust the path if the resource is in a different location
    URL resourceUrl = classLoader.getResource(resourceName);

    // Convert the URL to a Path
    if (resourceUrl != null) {
      return Paths.get(resourceUrl.getPath());
    } else {
      throw new IllegalArgumentException("Resource not found: " + resourceName);
    }
  }
}
