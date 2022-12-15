package ml.empee.mysticalBarriers.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ml.empee.configurator.ConfigFile;
import ml.empee.configurator.annotations.Path;
import org.bukkit.plugin.java.JavaPlugin;

@Getter @Setter(AccessLevel.PRIVATE)
public class Config extends ConfigFile {

  @Path("block-chorus-teleportation")
  private Boolean blockChorusFruitTeleportation = true;
  @Path("block-movement.projectiles.enabled")
  private Boolean blockProjectilesMovement = true;
  @Path("block-movement.projectiles.only-from-player")
  private Boolean blockProjectilesMovementOnlyFromPlayers = false;

  public Config(JavaPlugin plugin) {
    super(plugin, "config.yml");
  }

}
