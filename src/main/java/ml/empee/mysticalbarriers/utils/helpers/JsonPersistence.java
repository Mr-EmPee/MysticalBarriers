package ml.empee.mysticalbarriers.utils.helpers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JsonPersistence {
  private final Gson gson;
  @Getter
  private final File file;
  private final Lock writeLock = new ReentrantLock();

  public JsonPersistence(File file, Object... adapters) {
    this.file = file;

    GsonBuilder builder = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setPrettyPrinting()
        .disableHtmlEscaping();

    for(Object adapter : adapters) {
      builder.registerTypeAdapter(adapter.getClass(), adapter);
    }

    this.gson = builder.create();
  }

  /**
   * Thread-safe
   */
  public void serialize(Object object) {
    writeLock.lock();

    file.getParentFile().mkdirs();
    try (BufferedWriter w = Files.newBufferedWriter(file.toPath())) {
      w.append(gson.toJson(object));
    } catch (IOException e) {
      throw new JsonParseException("Error while serializing " + file.getName(), e);
    } finally {
      writeLock.unlock();
    }
  }


  /**
   * Thread-safe
   */
  @Nullable
  public <T> T deserialize(Class<T> clazz) {
    writeLock.lock();
    writeLock.unlock();

    try {
      T object = gson.fromJson(Files.newBufferedReader(file.toPath()), clazz);
      return object;
    } catch (NoSuchFileException e) {
      return null;
    } catch (JsonParseException e) {
      throw new JsonParseException("Misconfiguration of the file " + file, e);
    } catch (IOException e) {
      throw new JsonParseException("Error while deserializing the source " + file.toPath(), e);
    }
  }

  /**
   * Thread-safe
   */
  @NotNull
  public <T> List<T> deserializeList(Class<T[]> clazz) {
    T[] result = deserialize(clazz);
    if(result == null) {
      return new ArrayList<>();
    }

    return Arrays.asList(result);
  }

}
