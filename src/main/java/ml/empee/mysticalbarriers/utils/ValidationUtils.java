package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtils {

  public static <T> T has(Map<String, Object> map, String key, Class<T> type) {
    hasNonNull(map, key);
    return hasType(map, key, type).get();
  }

  public static <T> Optional<T> hasType(Map<String, Object> map, String key, Class<T> type) {
    return requireType(
      map.get(key), type,
      "Expected " + type.getName() + "type for field " + key + " but found " + map.get(key)
    );
  }

  public static void hasNonNull(Map<String, Object> map, String key) {
    requireNonNull(map.get(key), key);
  }

  public static <T> Optional<T> requireType(Object target, Class<T> type, @Nullable String message) {
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
    } else if(target.getClass() == Integer.class && type == Long.class) {
      return (Optional<T>) Optional.of(((Integer) target).longValue());
    } else if(target.getClass() == Float.class) {
      if(type == Double.class) {
        return (Optional<T>) Optional.of(((Float) target).doubleValue());
      } else if(type == Integer.class) {
        return (Optional<T>) Optional.of(((Float) target).intValue());
      } else if(type == Long.class) {
        return (Optional<T>) Optional.of(((Float) target).longValue());
      }
    }

    if (message == null) {
      throw new IllegalArgumentException("The object '" + target + "' must be a " + type.getName());
    } else {
      throw new IllegalArgumentException(message);
    }
  }

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
