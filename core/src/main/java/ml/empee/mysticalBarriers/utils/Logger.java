package ml.empee.mysticalBarriers.utils;

import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.commandsManager.command.Command;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Logger {

  public static final String PREFIX = "  &5MyB &8»&r ";
  private static final java.util.logging.Logger consoleLogger = JavaPlugin.getProvidingPlugin(
      MysticalBarriersPlugin.class).getLogger();

  static {
    Command.setPrefix(PREFIX + "&c");
  }

  public static Level getLevel() {
    return consoleLogger.getLevel();
  }

  public static void setLevel(Level level) {
    consoleLogger.setLevel(level);
  }

  public static void info(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.INFO)) {
      consoleLogger.info(String.format(Locale.ROOT, message, args));
    }
  }

  public static void warning(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.WARNING)) {
      consoleLogger.warning(String.format(Locale.ROOT, message, args));
    }
  }

  public static void error(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.SEVERE)) {
      consoleLogger.severe(String.format(Locale.ROOT, message, args));
    }
  }

  public static void debug(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.FINE)) {
      consoleLogger.info(String.format(Locale.ROOT, message, args));
    }
  }

  private static void log(CommandSender player, String message, ChatColor color, Object... args) {
    message = String.format(message, args);

    message = message.replace("\n", "\n&r");
    message = (PREFIX + message).replace("&r", color.toString());
    if (message.endsWith("\n")) {
      message += " ";
    }

    message = message.replace("\t", "    ");

    player.sendMessage(
        ChatColor.translateAlternateColorCodes('&', message).split("\n")
    );
  }

  public static void info(CommandSender player, String message, Object... args) {
    log(player, message, ChatColor.GRAY, args);
  }

  public static void warning(CommandSender player, String message, Object... args) {
    log(player, message, ChatColor.GOLD, args);
  }

  public static void error(CommandSender player, String message, Object... args) {
    log(player, message, ChatColor.RED, args);
  }

  public static void debug(CommandSender player, String message, Object... args) {
    if(consoleLogger.isLoggable(Level.FINE)) {
      log(player, message, ChatColor.DARK_GRAY, args);
    }
  }

}
