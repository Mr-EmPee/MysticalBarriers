package ml.empee.mysticalBarriers.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.gson.annotations.Expose;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ml.empee.mysticalBarriers.helpers.TriConsumer;
import ml.empee.mysticalBarriers.helpers.Tuple;
import ml.empee.mysticalBarriers.utils.LocationUtils;

public class Barrier {

  @Expose @Getter
  private final String id;

  @Expose @Getter
  private final Location firstCorner;

  @Expose @Getter
  private final Location secondCorner;

  @Expose @Getter @Setter
  private Material material;

  @Expose @Getter @Setter
  private int activationRange;

  @Expose @Getter @Setter
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

  private boolean isBarrierBlock(int x, int y, int z) {
    return
        x >= firstCorner.getBlockX() && x <= secondCorner.getBlockX()
        &&
        y >= firstCorner.getBlockY() && y <= secondCorner.getBlockY()
        &&
        z >= firstCorner.getBlockZ() && z <= secondCorner.getBlockZ();
  }

  public boolean isBarrierBlock(Location location) {
    if (!getWorld().equals(location.getWorld())) {
      return false;
    }

    return isBarrierBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  public boolean isBarrierChunk(int chunkX, int chunkZ) {
    if (chunks == null) {
      calculateChunks();
    }

    return chunks.contains(new Tuple<>(chunkX, chunkZ));
  }

  public boolean isHiddenFor(Player player) {
    return player.hasPermission("mysticalbarriers.bypass." + id);
  }

  public void forEachVisibleBarrierBlock(Location location, TriConsumer<Integer, Integer, Integer> consumer) {
    Objects.requireNonNull(location.getWorld());
    if (!location.getWorld().equals(getWorld())) {
      return;
    }

    LocationUtils.radiusSearch(location, activationRange, (x, y, z) -> {
      if (isBarrierChunk(x >> 4, z >> 4)) {
        if (location.getWorld().getBlockAt(x, y, z).getType() == Material.AIR) {
          if (isBarrierBlock(x, y, z)) {
            consumer.accept(x, y, z);
          }
        }
      }
    });
  }

  public World getWorld() {
    return firstCorner.getWorld();
  }

  private void calculateChunks() {
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
