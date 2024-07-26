package utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to send messages
 */

@UtilityClass
public class Messenger {

  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(Messenger.class);
  private final Logger consoleLogger = LoggerFactory.getLogger(plugin.getName());

  private final BukkitAudiences audience = BukkitAudiences.create(plugin);

  @Getter @Setter
  private String prefix = "";

  public void log(CommandSender sender, String msg, Object... obj) {
    msg = prefix + msg;

    for (Object o : obj) {
      msg = msg.replaceFirst("\\{}", o.toString());
    }

    audience.sender(sender).sendMessage(TextUtils.toComponent(msg));
  }

  public void log(String msg, Object... obj) {
    consoleLogger.info(msg, obj);
  }

  public void warn(String msg, Object... obj) {
    consoleLogger.warn(msg, obj);
  }

  public void error(String msg, Throwable throwable, Object... obj) {
    consoleLogger.error(msg, obj);
    throwable.printStackTrace();
  }

  public void error(String msg, Throwable exception) {
    consoleLogger.error(msg, exception);
  }

}
