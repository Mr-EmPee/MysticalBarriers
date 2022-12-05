package ml.empee.mysticalBarriers.model;

import com.google.gson.annotations.Expose;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ml.empee.mysticalBarriers.helpers.TriConsumer;
import ml.empee.mysticalBarriers.helpers.Tuple;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class Barrier {

  @Expose
  @Getter
  private final String id;

  @Expose
  @Getter
  private final Location firstCorner;

  @Expose
  @Getter
  private final Location secondCorner;

  @Expose
  @Getter
  @Setter
  private Material material;

  @Expose
  @Getter
  @Setter
  private int activationRange;

  @Expose
  @Getter
  @Setter
  private String blockData;

  private Set<Tuple<Integer, Integer>> chunks;

  @Builder
  public Barrier(String id, Location firstCorner, Location secondCorner) {
    this.id = id;
    this.material = Material.BARRIER;
    this.activationRange = 3;

    Tuple<Location, Location> corners = sortCorners(firstCorner, secondCorner);
    this.firstCorner = corners.getFirst();
    this.secondCorner = corners.getSecond();
  }

  private Tuple<Location, Location> sortCorners(Location firstCorner, Location secondCorner) {
    int minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
    int maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
    int minY = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
    int maxY = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
    int minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
    int maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());

    return new Tuple<>(
        new Location(firstCorner.getWorld(), minX, minY, minZ),
        new Location(secondCorner.getWorld(), maxX, maxY, maxZ)
    );
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

  public boolean isWithinBarrierRange(@Nullable World world, int x, int y, int z) {
    if (world != null && !getWorld().equals(world)) {
      return false;
    }

    boolean isWithinFirstCornerX = firstCorner.getBlockX() - activationRange<= x;
    boolean isWithinFirstCornerY = firstCorner.getBlockY() - activationRange <= y;
    boolean isWithinFirstCornerZ = firstCorner.getBlockZ() - activationRange <= z;

    boolean isWithinSecondCornerX = secondCorner.getBlockX() + activationRange >= x;
    boolean isWithinSecondCornerY = secondCorner.getBlockY() + activationRange >= y;
    boolean isWithinSecondCornerZ = secondCorner.getBlockZ() + activationRange >= z;

    return isWithinSecondCornerX && isWithinFirstCornerX
           && isWithinFirstCornerY && isWithinSecondCornerY
           && isWithinFirstCornerZ && isWithinSecondCornerZ;
  }

  public boolean isWithinBarrierRange(Location location) {
    return isWithinBarrierRange(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
