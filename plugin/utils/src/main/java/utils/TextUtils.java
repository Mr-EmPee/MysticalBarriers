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

  public String[] formatted(String text) {
    text = colorize(text);

    if (text.endsWith("\n")) {
      text += " ";
    }

    text = text.replace("\t", "    ");

    return text.split("\n");
  }

  public String[] description(String text) {
    if (!text.startsWith("\n")) {
      text = "\n" + text;
    }

    if (!text.endsWith("\n")) {
      text += "\n";
    }

    return formatted(text);
  }

}
