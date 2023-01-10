package ml.empee.mysticalBarriers.utils;

import java.util.Locale;
import java.util.logging.Level;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCLogger {

  private static final java.util.logging.Logger consoleLogger = JavaPlugin.getProvidingPlugin(MCLogger.class).getLogger();

  @Getter
  @Setter
  private static String prefix = "";
  @Getter
  @Setter
  private static boolean isDebugEnabled;

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
    if (isDebugEnabled) {
      consoleLogger.info(String.format(Locale.ROOT, message, args));
    }
  }

  private static void log(CommandSender player, String message, ChatColor color, Object... args) {
    message = String.format(message, args);

    message = message.replace("\n", "\n&r");
    message = (prefix + message).replace("&r", color.toString());
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
    if (isDebugEnabled) {
      log(player, message, ChatColor.DARK_GRAY, args);
    }
  }

}
