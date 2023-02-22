package ml.empee.mysticalbarriers.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ml.empee.configurator.Configuration;
import ml.empee.configurator.annotations.Path;
import ml.empee.configurator.annotations.Required;
import ml.empee.ioc.Bean;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Setter(AccessLevel.PRIVATE)
public class PluginConfig extends Configuration implements Bean {

  @Required
  @Path("block-chorus-teleportation")
  private Boolean blockChorusFruitTeleportation;

  @Required
  @Path("block-movement.projectiles")
  private MemorySection projectilesSettings;

  public PluginConfig(JavaPlugin plugin) {
    super(plugin, "config.yml", 1);
  }

  public boolean isProjectileMovementBlocked() {
    return projectilesSettings.getBoolean("enabled", true);
  }

  public boolean shouldBlockOnlyPlayerProjectiles() {
    return projectilesSettings.getBoolean("only-from-player", false);
  }


}
