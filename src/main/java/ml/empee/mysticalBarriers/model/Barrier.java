package ml.empee.mysticalBarriers.model;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ml.empee.json.validator.annotations.Required;
import ml.empee.json.validator.annotations.Validator;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import ml.empee.mysticalBarriers.utils.helpers.Tuple;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
public class Barrier {

  @Expose @Getter @Required
  private final String id;

  @Expose @Getter @Required
  private Location firstCorner;

  @Expose @Getter @Required
  private Location secondCorner;

  @Required @Expose @Getter @Setter
  private Material material;

  @Required @Expose @Getter @Setter
  private Integer activationRange;

  @Expose @Getter @Setter
  private String blockData;

  private Set<Tuple<Integer, Integer>> chunks;

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

    Location[] sortedLocations = LocationUtils.sortLocations(firstCorner, secondCorner);
    firstCorner = sortedLocations[0];
    secondCorner = sortedLocations[1];
  }

  @Validator
  private void validateBlockData() {
    if (blockData != null) {
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

  public boolean isBarrierAt(@Nullable World world, int x, int y, int z) {
    if (world != null && !getWorld().equals(world)) {
      return false;
    }

    return x >= firstCorner.getBlockX() && x <= secondCorner.getBlockX() && y >= firstCorner.getBlockY() &&
        y <= secondCorner.getBlockY() && z >= firstCorner.getBlockZ() && z <= secondCorner.getBlockZ();
  }

  public boolean isBarrierAt(Location location) {
    return isBarrierAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  public boolean isBarrierChunk(int chunkX, int chunkZ) {
    if (chunks == null) {
      computeBarrierChunks();
    }

    return chunks.contains(new Tuple<>(chunkX, chunkZ));
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

    return isWithinSecondCornerX && isWithinFirstCornerX && isWithinFirstCornerY && isWithinSecondCornerY &&
        isWithinFirstCornerZ && isWithinSecondCornerZ;
  }

  public boolean isWithinRange(Location location, @Nullable Integer range) {
    return isWithinRange(location.getWorld(), range, location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  public boolean isHiddenFor(Player player) {
    return player.hasPermission("mysticalbarriers.bypass." + id);
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

      if (isBarrierChunk(x >> 4, z >> 4)) {
        if (world.getBlockAt(x, y, z).getType() == Material.AIR) {
          if (isBarrierAt(null, x, y, z)) {
            consumer.accept(loc);
          }
        }
      }
    });
  }

  public World getWorld() {
    return firstCorner.getWorld();
  }

  private void computeBarrierChunks() {
    chunks = new HashSet<>();

    for (int chunkX = firstCorner.getBlockX() >> 4; chunkX <= secondCorner.getBlockX() >> 4; chunkX++) {
      for (int chunkZ = firstCorner.getBlockZ() >> 4; chunkZ <= secondCorner.getBlockZ() >> 4; chunkZ++) {
        chunks.add(new Tuple<>(chunkX, chunkZ));
      }
    }
  }
}
