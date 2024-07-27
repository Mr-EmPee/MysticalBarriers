package utils.files;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import utils.IReloadable;
import utils.Messenger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

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

  private final List<Migrator> migrators;

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

  public interface Migrator {
    /**
     * @return The version the migrator does produce in output
     */
    int migrate(int currentVersion, YamlConfiguration config);
  }

  /**
   * Copy and load the resource file from the JAR to the file system if it does not exist.
   */
  @SneakyThrows
  public ResourceConfig(JavaPlugin plugin, String resourcePath, boolean replace, List<Migrator> migrators) {
    this.file = new File(plugin.getDataFolder(), resourcePath);
    this.migrators = migrators;

    if (replace || !file.exists()) {
      Files.createDirectories(file.getParentFile().toPath());
      plugin.saveResource(resourcePath, true);

      Messenger.log("Extracted '{}' from JAR to plugin directory", resourcePath);
    }

    config = YamlConfiguration.loadConfiguration(file);
    updateConfiguration(plugin, resourcePath);
  }

  private void updateConfiguration(JavaPlugin plugin, String resourcePath) throws IOException {
    var defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(resourcePath)));

    int defaultVersion = defaultConfig.getInt("version", 1);
    int currentVersion = config.getInt("version", 1);

    if (currentVersion >= defaultVersion) {
      return;
    }

    Messenger.log("Updating config {} to version {}", file, defaultVersion);

    for (Migrator migrator : migrators) {
      currentVersion = migrator.migrate(currentVersion, config);
    }

    patch(config, defaultConfig);

    config.set("version", defaultVersion);
    config.save(file);
  }

  /**
   * Reloads the configuration file from the file system.
   */
  public void reload() {
    config = YamlConfiguration.loadConfiguration(file);
  }

}
