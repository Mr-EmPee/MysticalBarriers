package ml.empee.mysticalBarriers.utils.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializationUtils {

  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(SerializationUtils.class);
  private static final Gson gson;

  static {
    gson = new GsonBuilder()
        .registerTypeAdapter(Location.class, new LocationAdapter())
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting()
        .create();
  }

  public static void serializeAsync(Object object, String target) {
    Bukkit.getScheduler().runTaskAsynchronously(
        plugin, () -> serialize(object, target)
    );
  }
  public static void serialize(Object object, String target) {
    File file = new File(plugin.getDataFolder(), target);
    file.getParentFile().mkdirs();

    try(BufferedWriter w = Files.newBufferedWriter(file.toPath())) {
      w.append(gson.toJson(object));
    } catch (IOException e) {
      throw new MysticalBarrierException("Error while serializing " + target, e);
    }
  }


  @Nullable
  public static <T> T deserialize(String source, Class<T> clazz) {
    try {
      return gson.fromJson(Files.newBufferedReader(new File(plugin.getDataFolder(), source).toPath()), clazz);
    } catch (NoSuchFileException e) {
      return null;
    } catch (IOException e) {
      throw new MysticalBarrierException("Error while deserializing the source " + source, e);
    }
  }

  public static <T> void deserializeAsync(String source, Class<T> type, Consumer<T> consumer) {
    Bukkit.getScheduler().runTaskAsynchronously(
        plugin, () -> consumer.accept( deserialize(source, type) )
    );
  }

}
