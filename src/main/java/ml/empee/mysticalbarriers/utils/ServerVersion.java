package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

/**
 * Utility class to compare the server version.
 **/

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerVersion {

  private static final int[] VERSION;

  static {
    String[] rawVersioning = Bukkit.getBukkitVersion().split("-")[0].split("\\.");

    VERSION = new int[rawVersioning.length];

    for (int i = 0; i < VERSION.length; i++) {
      VERSION[i] = Integer.parseInt(rawVersioning[i]);
    }
  }

  /**
   * Check if the server version is greater or equals than the given version.
   */
  public static boolean isGreaterThan(int... v) {

    for (int i = 0; i < v.length; i++) {
      int work = v[i] - VERSION[i];

      if (work > 0) {
        return false;
      } else if (work < 0) {
        break;
      }

    }

    return true;

  }

  /**
   * Check if the server version is lower than the given version.
   */
  public static boolean isLowerThan(int... v) {
    return !isGreaterThan(v);
  }

}
