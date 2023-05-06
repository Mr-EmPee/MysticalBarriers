package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set of utilities method used to work with locations.
 **/

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationUtils {

  /**
   * Check if 2 locations represent the same block.
   **/
  public static boolean isSameBlock(@Nullable Location first, @Nullable Location second) {
    if (first == null || second == null) {
      return false;
    }

    return first.getBlockX() - second.getBlockX() == 0
        && first.getBlockY() - second.getBlockY() == 0
        && first.getBlockZ() - second.getBlockZ() == 0;
  }

  public static Location toBlockLoc(Location target) {
    return new Location(target.getWorld(), target.getBlockX(), target.getBlockY(), target.getBlockZ());
  }

  /**
   * Get all the blocks within a radius.
   **/
  public static List<Location> getBlocksWithin(Location location, int radius) {
    int maxX = location.getBlockX() + radius;
    int maxY = location.getBlockY() + radius;
    int maxZ = location.getBlockZ() + radius;
    int minX = location.getBlockX() - radius;
    int minY = location.getBlockY() - radius;
    int minZ = location.getBlockZ() - radius;

    List<Location> locations = new ArrayList<>();
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        for (int z = minZ; z <= maxZ; z++) {
          locations.add(new Location(location.getWorld(), x, y, z));
        }
      }
    }

    return locations;
  }

  public static Vector getDistance(Location first, Location second) {
    int dx = first.getBlockX() - second.getBlockX();
    int dy = first.getBlockY() - second.getBlockY();
    int dz = first.getBlockZ() - second.getBlockZ();

    return new Vector(Math.abs(dx), Math.abs(dy), Math.abs(dz));
  }

  /**
   * Get a list of all the adjacent blocks.
   **/
  public static List<Location> getAdjacentBlocks(Location center) {
    center = center.getBlock().getLocation();

    return Arrays.asList(
        center,
        center.clone().add(1, 0, 0),
        center.clone().add(0, 1, 0),
        center.clone().add(0, 0, 1),
        center.clone().add(-1, 0, 0),
        center.clone().add(0, -1, 0),
        center.clone().add(0, 0, -1)
    );
  }

  /**
   * Get the blocks between the starting location and the ending location.
   **/
  public static List<Location> getBlocksArea(Location first, Location second) {
    Vector start = Vector.getMinimum(first.toVector(), second.toVector());
    Vector end = Vector.getMaximum(first.toVector(), second.toVector());

    ArrayList<Location> locations = new ArrayList<>();
    for (int x = start.getBlockX(); x <= end.getBlockX(); x++) {
      for (int y = start.getBlockY(); y <= end.getBlockY(); y++) {
        for (int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {
          locations.add(new Location(first.getWorld(), x, y, z));
        }
      }
    }

    return locations;
  }

}
