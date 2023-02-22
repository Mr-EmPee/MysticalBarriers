package ml.empee.mysticalbarriers.utils.helpers;

import java.util.Locale;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.MysticalBarriersPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class allow you to easily log messages.
 **/

public final class Logger implements Bean {

  @Getter
  @Setter
  private String prefix;
  @Getter
  @Setter
  private boolean isDebugEnabled;

  @Setter
  private java.util.logging.Logger consoleLogger;

  public Logger(JavaPlugin plugin) {
    prefix = MysticalBarriersPlugin.PREFIX;
    consoleLogger = plugin.getLogger();
  }

  private void log(CommandSender player, String message, ChatColor color, Object... args) {
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

  /**
   * Log to the console a debug message.
   **/
  public void debug(String message, Object... args) {
    if (isDebugEnabled) {
      consoleLogger.info(String.format(Locale.ROOT, message, args));
    }
  }

  /**
   * Log a debug message to a player.
   **/
  public void debug(CommandSender player, String message, Object... args) {
    if (isDebugEnabled) {
      log(player, message, ChatColor.DARK_GRAY, args);
    }
  }

  /**
   * Log to the console an info message.
   **/
  public void info(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.INFO)) {
      consoleLogger.info(String.format(Locale.ROOT, message, args));
    }
  }

  /**
   * Log an info message to a player.
   **/
  public void info(CommandSender player, String message, Object... args) {
    log(player, message, ChatColor.GRAY, args);
  }

  /**
   * Log to the console a warning message.
   **/
  public void warning(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.WARNING)) {
      consoleLogger.warning(String.format(Locale.ROOT, message, args));
    }
  }

  /**
   * Log a warning message to a player.
   **/
  public void warning(CommandSender player, String message, Object... args) {
    log(player, message, ChatColor.GOLD, args);
  }

  /**
   * Log to the console an error message.
   **/
  public void error(String message, Object... args) {
    if (consoleLogger.isLoggable(Level.SEVERE)) {
      consoleLogger.severe(String.format(Locale.ROOT, message, args));
    }
  }

  /**
   * Log an error message to a player.
   **/
  public void error(CommandSender player, String message, Object... args) {
    log(player, message, ChatColor.RED, args);
  }
}
