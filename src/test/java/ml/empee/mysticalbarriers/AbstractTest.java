package ml.empee.mysticalbarriers;

import com.google.common.io.Files;
import com.google.gson.Gson;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTest {

  public File getResource() {
    return new File("src/test/resources");
  }

  @SneakyThrows
  public <T> T parseFile(String filePath, Class<T> tClass) {
    File file = new File(getResource(), filePath);
    return new Gson().fromJson(Files.newReader(file, Charset.defaultCharset()), tClass);
  }

  /**
   * Try to run the runnable every 0.5 seconds
   */
  @SneakyThrows
  public void waitAtMostUntilAsserted(
      int time, TimeUnit unit, Runnable runnable
  ) {
    long startTime = System.currentTimeMillis();
    long maxTime = unit.toMillis(time);
    Exception lastError = null;
    while (System.currentTimeMillis() - startTime < maxTime) {
      try {
        runnable.run();
        lastError = null;
        break;
      } catch (Exception e) {
        lastError = e;
        Thread.sleep(500);
      }
    }

    if (lastError != null) {
      throw lastError;
    }
  }

}
