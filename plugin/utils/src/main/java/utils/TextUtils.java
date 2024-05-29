package utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.Map;

/**
 * Utility class to format minecraft text
 */

@UtilityClass
public class TextUtils {

  public String colorize(String text) {
    return ChatColor.translateAlternateColorCodes('&', text);
  }

  public String[] formatted(String text) {
    return formatted(text, Collections.emptyMap());
  }

  public String[] formatted(String text, Map<String, Object> placeholders) {
    for (var placeholder : placeholders.entrySet()) {
      text = text.replace("{" + placeholder.getKey() + "}", placeholder.getValue().toString());
    }

    text = colorize(text);
    if (text.endsWith("\n")) {
      text += " ";
    }

    text = text.replace("\t", "    ");

    return text.split("\n");
  }

  public String[] centered(String text) {
    if (!text.startsWith("\n")) {
      text = "\n" + text;
    }

    if (!text.endsWith("\n")) {
      text += "\n";
    }

    return formatted(text);
  }

  public String[] centered(String text, Map<String, Object> placeholders) {
    if (!text.startsWith("\n")) {
      text = "\n" + text;
    }

    if (!text.endsWith("\n")) {
      text += "\n";
    }

    return formatted(text, placeholders);
  }

}
