package ml.empee.mysticalbarriers.config;

import ml.empee.ioc.Bean;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Barriers related general configs
 */

public class BarriersConfig implements Bean {

  private static final int LATEST_VERSION = 2;
  private final File file;

  private ConfigurationSection config;

  public BarriersConfig(JavaPlugin plugin) throws IOException {
    file = new File(plugin.getDataFolder(), "config.yml");
    if (!file.exists()) {
      plugin.saveResource("config.yml", true);
    }

    this.config = loadConfig(file);
  }

  private YamlConfiguration loadConfig(File file) throws IOException {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    boolean hasBeenUpdated = updateConfig(config.getInt("version", 1));
    if (hasBeenUpdated) {
      config.save(file);
    }

    return config;
  }

  public void reload() throws IOException {
    config = loadConfig(file);
  }

  private boolean updateConfig(int currentVersion) {
    if (currentVersion == LATEST_VERSION) {
      return false;
    }

    updateToV2(currentVersion);

    return true;
  }

  private void updateToV2(int version) {
    if (version != 1) {
      return;
    }

    ConfigurationSection section = config.createSection("block-entities");
    section.set("enabled", true);
    section.set("exclude", List.of("ARMOR_STAND"));

    section = config.createSection("repel-projectiles");
    section.set("enabled", config.get("block-movement.projectiles.enabled", true));
    section.set("only-from-player", config.get("block-movement.projectiles.only-from-player", false));

    config.set("block-movement", null);
  }

  public boolean shouldBlockChorusTp() {
    return config.getBoolean("block-chorus-teleportation", true);
  }

  public boolean shouldBlockEntities() {
    return config.getBoolean("block-entities.enabled", true);
  }

  public boolean shouldRepelProjectiles() {
    return config.getBoolean("repel-projectiles.enabled", true);
  }

  public boolean shouldRepelOnlyPlayerProjectiles() {
    return config.getBoolean("repel-projectiles.only-from-player", false);
  }

}
