package ml.empee.mysticalbarriers.config;

import ml.empee.ioc.Bean;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Barriers related general configs
 */

public class BarriersConfig extends AbstractConfig implements Bean {

  public BarriersConfig(JavaPlugin plugin) {
    super(plugin, "config.yml", 2);
  }

  protected void update(int from) {
    updateToV2(from);
  }

  private void updateToV2(int version) {
    if (version >= 2) {
      return;
    }

    ConfigurationSection section = config.createSection("block-entities");
    section.set("enabled", true);

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
