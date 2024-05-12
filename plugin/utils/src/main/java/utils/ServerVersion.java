package utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerVersion {

  private static final int[] VERSION;

  static {
    String[] rawVersioning = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");

    VERSION = new int[rawVersioning.length];

    for (int i = 0; i < VERSION.length; i++) {
      VERSION[i] = Integer.parseInt(rawVersioning[i]);
    }
  }

  /**
   * @return <b>true</b> if the server's version is greater or equals
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
   * @return <b>true</b> if the server's version is lower
   */
  public static boolean isLowerThan(int... v) {
    return !isGreaterThan(v);
  }

}