package utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

/**
 * Utility class to format minecraft text
 */

@UtilityClass
public class TextUtils {

  public String colorize(String text) {
    return ChatColor.translateAlternateColorCodes('&', text);
  }

}
