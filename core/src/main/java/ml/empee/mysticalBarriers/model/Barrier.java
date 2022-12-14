package ml.empee.mysticalBarriers.model;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ml.empee.mysticalBarriers.helpers.TriConsumer;
import ml.empee.mysticalBarriers.helpers.Tuple;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import ml.empee.mysticalBarriers.utils.serialization.annotations.Required;
import ml.empee.mysticalBarriers.utils.serialization.annotations.Validated;
import ml.empee.mysticalBarriers.utils.serialization.annotations.Validator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Validated
public class Barrier {

  @Expose @Required @Getter
  private final String id;

  @Expose @Required @Getter
  private Location firstCorner;

  @Expose @Required @Getter
  private Location secondCorner;

  @Expose @Required
  @Getter @Setter
  private Material material;

  @Expose @Required @Getter @Setter
  private Integer activationRange;

  @Expose @Getter @Setter
  private String blockData;

  private Set<Tuple<Integer, Integer>> chunks;

  @Builder
  public Barrier(String id, Location firstCorner, Location secondCorner) {
    this.id = id;
    this.material = Material.BARRIER;
    this.activationRange = 3;
    this.firstCorner = firstCorner;
    this.secondCorner = secondCorner;

    validateCorners();
  }

  @Validator
  private void validateCorners() {
    int minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
    int maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
    int minY = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
    int maxY = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
    int minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
    int maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());

    this.firstCorner = new Location(firstCorner.getWorld(), minX, minY, minZ);
    this.secondCorner = new Location(secondCorner.getWorld(), maxX, maxY, maxZ);

    if (firstCorner.getWorld() == null || secondCorner.getWorld() == null) {
      throw new JsonParseException("A corner world of the barrier " + id + " is null or doesn't exist");
    } else if(!firstCorner.getWorld().equals(secondCorner.getWorld())) {
      throw new JsonParseException("The two corners of the barrier " + id + " must be in the same world");
    }
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

  public boolean existsBarrierAt(@Nullable World world, int x, int y, int z) {
    if (world != null && !getWorld().equals(world)) {
      return false;
    }

    return
        x >= firstCorner.getBlockX() && x <= secondCorner.getBlockX()
        &&
        y >= firstCorner.getBlockY() && y <= secondCorner.getBlockY()
        &&
        z >= firstCorner.getBlockZ() && z <= secondCorner.getBlockZ();
  }

  public boolean existsBarrierAt(Location location) {
    return existsBarrierAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
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

    return isWithinSecondCornerX && isWithinFirstCornerX
           && isWithinFirstCornerY && isWithinSecondCornerY
           && isWithinFirstCornerZ && isWithinSecondCornerZ;
  }

  public boolean isWithinRange(Location location, @Nullable Integer range) {
    return isWithinRange(
        location.getWorld(), range,
        location.getBlockX(), location.getBlockY(), location.getBlockZ()
    );
  }

  public boolean isHiddenFor(Player player) {
    return player.hasPermission("mysticalbarriers.bypass." + id);
  }

  public void forEachVisibleBarrierBlock(Location location, TriConsumer<Integer, Integer, Integer> consumer) {
    if (!getWorld().equals(location.getWorld())) {
      return;
    }

    LocationUtils.radiusSearch(location, activationRange, (x, y, z) -> {
      if (isBarrierChunk(x >> 4, z >> 4)) {
        if (location.getWorld().getBlockAt(x, y, z).getType() == Material.AIR) {
          if (existsBarrierAt(null, x, y, z)) {
            consumer.accept(x, y, z);
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Barrier barrier = (Barrier) o;

    return id.equals(barrier.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

}
