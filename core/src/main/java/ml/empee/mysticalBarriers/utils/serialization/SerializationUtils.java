package ml.empee.mysticalBarriers.utils.serialization;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.mysticalBarriers.utils.serialization.annotations.Required;
import ml.empee.mysticalBarriers.utils.serialization.annotations.Validated;
import ml.empee.mysticalBarriers.utils.serialization.annotations.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

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
        .disableHtmlEscaping()
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

    try (BufferedWriter w = Files.newBufferedWriter(file.toPath())) {
      w.append(gson.toJson(object));
    } catch (IOException e) {
      throw new JsonParseException("Error while serializing " + target, e);
    }
  }

  @Nullable
  public static <T> T deserialize(String source, Class<T> clazz) {
    try {
      T object = gson.fromJson(Files.newBufferedReader(new File(plugin.getDataFolder(), source).toPath()), clazz);
      checkObject(object);
      return object;
    } catch (NoSuchFileException e) {
      return null;
    } catch (JsonParseException e) {
      throw new JsonParseException("The file " + source + " is misconfigured. " + e.getMessage(), e);
    } catch (IOException e) {
      throw new JsonParseException("Error while deserializing the source " + source, e);
    }
  }

  public static <T> void deserializeAsync(String source, Class<T> type, Consumer<T> consumer) {
    Bukkit.getScheduler().runTaskAsynchronously(
        plugin, () -> consumer.accept(deserialize(source, type))
    );
  }

  private static void runValidationCheckMethods(Object pojo) throws JsonParseException {
    List<Method> methods = Arrays.stream(pojo.getClass().getDeclaredMethods())
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> m.getAnnotation(Validator.class) != null)
        .collect(Collectors.toList());

    for(Method method : methods) {
      method.setAccessible(true);

      try {
        method.invoke(pojo);
      } catch (IllegalAccessException | InvocationTargetException e) {
        if(e.getCause() instanceof JsonParseException) {
          throw (JsonParseException) e.getCause();
        }

        throw new JsonParseException(e);
      }
    }
  }

  private static void checkRequiredFields(Field[] fields, Object pojo) throws JsonParseException {
    for (Field f : fields) {
      // If some field has required annotation.
      if (f.getAnnotation(Required.class) != null) {
        try {
          f.setAccessible(true);
          Object fieldObject = f.get(pojo);
          if (fieldObject == null) {
            throw new JsonParseException(String.format("The field %s can't be null", f.getName()));
          }
        }

        // Exceptions while reflection.
        catch (IllegalArgumentException | IllegalAccessException e) {
          throw new JsonParseException(e);
        }
      }
    }
  }

  private static void checkObject(Object pojo) throws JsonParseException {
    if (pojo instanceof List || pojo.getClass().isArray()) {
      Object[] pojoList;
      if(pojo instanceof List) {
        pojoList = ((List<?>) pojo).toArray();
      } else {
        pojoList = (Object[]) pojo;
      }

      for (final Object pojoListPojo : pojoList) {
        checkObject(pojoListPojo);
      }
    } else {
      Class<?> clazz = pojo.getClass();
      do {
        if(clazz.getAnnotation(Validated.class) != null) {
          Field[] fields = clazz.getDeclaredFields();
          checkRequiredFields(fields, pojo);
          runValidationCheckMethods(pojo);
        }
      } while ((clazz = clazz.getSuperclass()) != null);
    }
  }


}
