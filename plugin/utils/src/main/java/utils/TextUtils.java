package utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

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
    text = text.replace("\t", "    ");

    return text.split("\n");
  }

}
