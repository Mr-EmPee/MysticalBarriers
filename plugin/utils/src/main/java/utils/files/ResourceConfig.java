package utils.files;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import utils.IReloadable;
import utils.Messenger;

import java.io.File;
import java.io.InputStreamReader;
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

  private static void patch(ConfigurationSection target, ConfigurationSection patch) {
    for (String key : patch.getKeys(false)) {
      if (!target.contains(key)) {
        target.set(key, patch.get(key));
        continue;
      }

      var patchedValue = patch.get(key);
      if (patchedValue instanceof ConfigurationSection) {
        patch((ConfigurationSection) target.get(key), (ConfigurationSection) patchedValue);
      }
    }
  }

  /**
   * Copy and load the resource file from the JAR to the file system if it does not exist.
   */
  @SneakyThrows
  public ResourceConfig(JavaPlugin plugin, String path, boolean replace, int version) {
    this.file = new File(plugin.getDataFolder(), path);

    if (replace || !file.exists()) {
      Files.createDirectories(file.getParentFile().toPath());
      plugin.saveResource(path, true);

      Messenger.log("Extracted '{}' from JAR to plugin directory", path);
    }

    config = YamlConfiguration.loadConfiguration(file);
    if (getVersion() < version) {
      Messenger.log("Updating config {} to version {}", file, version);
      migrate(version);

      var patchConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(path)));
      patch(config, patchConfig);

      config.set("version", version);
      config.save(file);
    }
  }

  protected void migrate(int targetVersion) {

  }

  public int getVersion() {
    return config.getInt("version", 1);
  }

  /**
   * Reloads the configuration file from the file system.
   */
  public void reload() {
    config = YamlConfiguration.loadConfiguration(file);
  }

}
