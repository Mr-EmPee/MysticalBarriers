package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.logging.Level;

/**
 * This class allow you to easily log messages.
 **/

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Logger {

  @Getter @Setter
  private static String prefix;
  @Getter @Setter
  private static boolean isDebugEnabled;
  @Setter
  private static java.util.logging.Logger consoleLogger = JavaPlugin.getProvidingPlugin(Logger.class).getLogger();

  public static void log(CommandSender sender, String message, Object... args) {
    message = String.format(message, args);

    message = message.replace("\n", "\n&r");
    message = prefix + message;
    if (message.endsWith("\n")) {
      message += " ";
    }

    message = message.replace("\t", "    ");

    sender.sendMessage(
        ChatColor.translateAlternateColorCodes('&', message).split("\n")
    );
  }

  public static void translatedLog(CommandSender sender, String key, Object... args) {
    log(sender, Translator.translate(key), args);
  }

  /** Log to the console a debug message. **/
  public static void debug(String message, Object... args) {
    if (isDebugEnabled) {
      consoleLogger.info(String.format(Locale.ROOT, message, args));
    }
  }

  /** Log a debug message to a player. **/
  public static void debug(CommandSender player, String message, Object... args) {
    if (isDebugEnabled) {
      log(player, message, ChatColor.DARK_GRAY, args);
    }
  }

  /** Log to the console an info message. **/
  public static void info(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.INFO)) {
      consoleLogger.info(String.format(Locale.ROOT, message, args));
    }
  }

  /** Log to the console a warning message. **/
  public static void warning(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.WARNING)) {
      consoleLogger.warning(String.format(Locale.ROOT, message, args));
    }
  }

  /** Log to the console an error message. **/
  public static void error(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.SEVERE)) {
      consoleLogger.severe(String.format(Locale.ROOT, message, args));
    }
  }
}
