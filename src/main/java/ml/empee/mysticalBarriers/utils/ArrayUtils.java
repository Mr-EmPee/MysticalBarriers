package ml.empee.mysticalBarriers.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayUtils {
  public static short[] toPrimitive(Collection<Short> collection) {
    short[] result = new short[collection.size()];

    int i = 0;
    for (Short value : collection) {
      result[i] = value;
      i++;
    }

    return result;
  }

  @NotNull
  public static <T> List<T> toList(@Nullable T[] array) {
    ArrayList<T> result = new ArrayList<>();

    if (array != null) {
      Collections.addAll(result, array);
    }

    return result;
  }

}
