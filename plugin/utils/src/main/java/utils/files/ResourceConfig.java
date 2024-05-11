package utils.files;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import utils.IReloadable;
import utils.Messenger;

import java.io.File;
import java.nio.file.Files;

/**
 * This class facilitates the management of a resource file typically used for configuration
 * purposes in Java applications. It allows the file to be copied from the application's JAR
 * to the file system, enabling users to modify the configuration settings without altering
 * the original resource bundled within the JAR.
 **/

public class ResourceConfig implements IReloadable {

  @Getter
  private final File file;

  @Getter
  private YamlConfiguration config;

  /**
   * Copy and load the resource file from the JAR to the file system if it does not exist.
   */
  @SneakyThrows
  public ResourceConfig(JavaPlugin plugin, String path, boolean replace) {
    this.file = new File(plugin.getDataFolder(), path);

    if (replace || !file.exists()) {
      Files.createDirectories(file.getParentFile().toPath());
      plugin.saveResource(path, true);

      Messenger.log("Extracted '{}' from JAR to plugin directory", path);
    }

    this.config = YamlConfiguration.loadConfiguration(file);
  }

  /**
   * Reloads the configuration file from the file system.
   */
  public void reload() {
    config = YamlConfiguration.loadConfiguration(file);
  }

}
