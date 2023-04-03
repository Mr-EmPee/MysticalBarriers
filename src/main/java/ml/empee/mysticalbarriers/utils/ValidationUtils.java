package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Utility to validate some data type with some pre-configured messages when an error arise
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtils {

  public static <T> T get(Map<String, Object> map, String key, Class<T> type) {
    has(map, key);
    return getOrEmpty(map, key, type).get();
  }

  public static <T> Optional<T> getOrEmpty(Map<String, Object> map, String key, Class<T> type) {
    return requireType(
        map.get(key), type,
        "Expected " + type.getName() + "type for field " + key + " but found " + map.get(key)
    );
  }

  public static void has(Map<String, Object> map, String key) {
    requireNonNull(map.get(key), key);
  }

  /**
   * Validate that the target data can be assigned to the required type
   */
  @SuppressWarnings("checkstyle:CyclomaticComplexity")
  public static <T> Optional<T> requireType(Object target, Class<T> type, @Nullable String message) {
    //Hard-coded solution for when the data type doesn't correspond to the requested type
    //but it can be easily converter
    if (target == null) {
      return Optional.empty();
    } else if (type.isAssignableFrom(target.getClass())) {
      return (Optional<T>) Optional.of(target);
    } else if (target.getClass() == Double.class) {
      if (type == Integer.class) {
        return (Optional<T>) Optional.of(((Double) target).intValue());
      } else if (type == Long.class) {
        return (Optional<T>) Optional.of(((Double) target).longValue());
      } else if (type == Float.class) {
        return (Optional<T>) Optional.of(((Double) target).floatValue());
      }
    } else if (target.getClass() == Integer.class && type == Long.class) {
      return (Optional<T>) Optional.of(((Integer) target).longValue());
    } else if (target.getClass() == Float.class) {
      if (type == Double.class) {
        return (Optional<T>) Optional.of(((Float) target).doubleValue());
      } else if (type == Integer.class) {
        return (Optional<T>) Optional.of(((Float) target).intValue());
      } else if (type == Long.class) {
        return (Optional<T>) Optional.of(((Float) target).longValue());
      }
    }

    if (message == null) {
      throw new IllegalArgumentException("The object '" + target + "' must be a " + type.getName());
    } else {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Validate that the target data can be assigned to the required type
   */
  public static <T> Optional<T> requireType(Object target, Class<T> type) {
    return requireType(target, type, null);
  }

  public static void requireNonNull(Object target, String fieldName) {
    if (target != null) {
      return;
    }

    throw new IllegalArgumentException("Missing required field '" + fieldName + "'");
  }

}
