package ml.empee.mysticalBarriers.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ml.empee.configurator.Configuration;
import ml.empee.configurator.annotations.Path;
import ml.empee.configurator.annotations.Required;
import ml.empee.ioc.annotations.Bean;
import org.bukkit.configuration.MemorySection;

@Bean
@Getter @Setter(AccessLevel.PRIVATE)
public class Config extends Configuration {

  @Required
  @Path("block-chorus-teleportation")
  private Boolean blockChorusFruitTeleportation;

  @Required
  @Path("block-movement.projectiles")
  private MemorySection projectilesSettings;

  public Config() {
    super("config.yml", 1);
  }

  public boolean isProjectileMovementBlocked() {
    return projectilesSettings.getBoolean("enabled", true);
  }

  public boolean shouldBlockOnlyPlayerProjectiles() {
    return projectilesSettings.getBoolean("only-from-player", false);
  }


}
