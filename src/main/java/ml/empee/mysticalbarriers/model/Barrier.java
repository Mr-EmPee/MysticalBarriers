package ml.empee.mysticalbarriers.model;

import com.google.gson.JsonParseException;
import java.util.function.Consumer;
import lombok.Data;
import ml.empee.json.validator.annotations.Required;
import ml.empee.json.validator.annotations.Validator;
import ml.empee.mysticalbarriers.Permissions;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Data
public class Barrier {

  @Required
  private String id;
  @Required
  private Location firstCorner;
  @Required
  private Location secondCorner;
  @Required
  private Material material;
  @Required
  private Integer activationRange;
  private String blockData;

  public Barrier(String id) {
    this.id = id;
    this.material = Material.BARRIER;
    this.activationRange = 3;
  }

  @Validator
  private void validateCorners() {
    if (firstCorner.getWorld() == null || secondCorner.getWorld() == null) {
      throw new JsonParseException("A corner world of the barrier " + id + " is null or doesn't exist");
    } else if (!firstCorner.getWorld().equals(secondCorner.getWorld())) {
      throw new JsonParseException("The two corners of the barrier " + id + " must be in the same world");
    }

    Location temp = LocationUtils.findLowestPoint(firstCorner, secondCorner);
    secondCorner = LocationUtils.findGreatestPoint(firstCorner, secondCorner);
    firstCorner = temp;

  }

  @Validator
  private void validateBarrierMaterial() {
    if (!material.isBlock()) {
      throw new JsonParseException("The material of the barrier " + id + " is not a block");
    } else if (blockData != null) {
      try {
        material.createBlockData(blockData);
      } catch (IllegalArgumentException e) {
        throw new JsonParseException("Invalid block data of the barrier " + id);
      }
    }
  }

  @Validator
  private void validateActivationRange() {
    if (activationRange <= 0) {
      throw new JsonParseException("Activation range of the barrier " + id + " cannot be lower then 1");
    }
  }

  public void setFirstCorner(Location firstCorner) {
    this.firstCorner = firstCorner;
    if (secondCorner != null) {
      validateCorners();
    }
  }

  public void setSecondCorner(Location secondCorner) {
    this.secondCorner = secondCorner;
    if (firstCorner != null) {
      validateCorners();
    }
  }

  public void setCorners(Location firstCorner, Location secondCorner) {
    this.firstCorner = firstCorner;
    this.secondCorner = secondCorner;
    validateCorners();
  }

  public boolean isBarrierAt(@Nullable World world, int x, int y, int z) {
    if (world != null && !getWorld().equals(world)) {
      return false;
    }

    return x >= firstCorner.getBlockX()
      && x <= secondCorner.getBlockX()
      && y >= firstCorner.getBlockY()
      && y <= secondCorner.getBlockY()
      && z >= firstCorner.getBlockZ()
      && z <= secondCorner.getBlockZ();
  }

  public boolean isBarrierAt(Location location) {
    return isBarrierAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  /**
   * @param world if not null checks even the world
   * @param range if null use the barrier activation range
   */
  public boolean isWithinRange(@Nullable World world, @Nullable Integer range, int x, int y, int z) {
    if (world != null && !getWorld().equals(world)) {
      return false;
    }

    if (range == null) {
      range = activationRange;
    }

    boolean isWithinFirstCornerX = firstCorner.getBlockX() - range <= x;
    boolean isWithinFirstCornerY = firstCorner.getBlockY() - range <= y;
    boolean isWithinFirstCornerZ = firstCorner.getBlockZ() - range <= z;

    boolean isWithinSecondCornerX = secondCorner.getBlockX() + range >= x;
    boolean isWithinSecondCornerY = secondCorner.getBlockY() + range >= y;
    boolean isWithinSecondCornerZ = secondCorner.getBlockZ() + range >= z;

    return isWithinSecondCornerX
      && isWithinFirstCornerX
      && isWithinFirstCornerY
      && isWithinSecondCornerY
      && isWithinFirstCornerZ
      && isWithinSecondCornerZ;
  }

  public boolean isWithinRange(Location location, @Nullable Integer range) {
    return isWithinRange(
      location.getWorld(), range, location.getBlockX(), location.getBlockY(), location.getBlockZ()
    );
  }

  public boolean isHiddenFor(Player player) {
    return player.hasPermission(Permissions.BYPASS_PERMISSION + id);
  }

  public void forEachVisibleBarrierBlock(Location location, Consumer<Location> consumer) {
    if (!getWorld().equals(location.getWorld())) {
      return;
    }

    World world = location.getWorld();
    LocationUtils.radiusSearch(location, activationRange, loc -> {
      int x = loc.getBlockX();
      int y = loc.getBlockY();
      int z = loc.getBlockZ();

      if (world.getBlockAt(x, y, z).getType() == Material.AIR) {
        if (isBarrierAt(null, x, y, z)) {
          consumer.accept(loc);
        }
      }
    });
  }

  public World getWorld() {
    return firstCorner.getWorld();
  }
}
