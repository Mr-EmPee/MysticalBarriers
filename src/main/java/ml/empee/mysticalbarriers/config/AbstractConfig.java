package ml.empee.mysticalbarriers.config;

import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Generic class to cache and update a configuration file
 */

public abstract class AbstractConfig {

  protected final File file;
  protected final int version;
  protected YamlConfiguration config;

  public AbstractConfig(JavaPlugin plugin, String resource, int version) {
    file = new File(plugin.getDataFolder(), resource);
    if (!file.exists()) {
      plugin.saveResource(resource, true);
    }

    this.version = version;
    this.config = loadConfig(file);
  }

  private YamlConfiguration loadConfig(File file) {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    update();

    return config;
  }

  @SneakyThrows
  private void update() {
    int currentVersion = config.getInt("version", 1);
    if (currentVersion == version) {
      return;
    } else if (currentVersion > version) {
      throw new IllegalArgumentException("The config file has been generated from a newer version!");
    }

    update(currentVersion);

    config.save(file);
  }

  protected abstract void update(int from);

  public void reload() {
    config = loadConfig(file);
  }

}
