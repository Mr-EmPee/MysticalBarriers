package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Set of utilities method used to work with locations. **/

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationUtils {

  /** Check if 2 locations represent the same block. **/
  public static boolean isSameBlock(@NotNull Location first, @NotNull Location second) {
    return first.getBlockX() - second.getBlockX() == 0
      && first.getBlockY() - second.getBlockY() == 0
      && first.getBlockZ() - second.getBlockZ() == 0;
  }

  /** Get all the blocks within a radius. **/
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

  /**
   * Get the major distance between the the x,y,z axis of the two locations.
   * <br><br>
   * <b>Example:</b> <br>
   * <ul>
   * <li>Location 1: 0, -20, 0</li>
   * <li>Location 2: 10, 0, -13</li>
   * </ul>
   * this method will return 20
   */
  public static int getGreatestAxisDistance(Location first, Location second) {
    int dx = first.getBlockX() - second.getBlockX();
    int dy = first.getBlockY() - second.getBlockY();
    int dz = first.getBlockZ() - second.getBlockZ();

    return Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));
  }

  /** Get a list of all the adjacent blocks. **/
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
   * Find the greatest point given two locations.
   * <br><br>
   * <b>Example:</b> <br>
   * <ul>
   *  <li>Location 1: 10, -2, 3</li>
   *  <li>Location 2: 0, 19, 2</li>
   * </ul>
   * this method will return 10, 19, 3
   */
  public static Location findGreatestPoint(Location first, Location second) {
    if (first.getWorld() != second.getWorld()) {
      throw new IllegalArgumentException("Locations must be in the same world");
    }

    double maxX = Math.max(first.getX(), second.getX());
    double maxY = Math.max(first.getY(), second.getY());
    double maxZ = Math.max(first.getZ(), second.getZ());

    return new Location(second.getWorld(), maxX, maxY, maxZ);
  }

  /**
   * Find the lowest point given two locations.
   * <br><br>
   * <b>Example:</b> <br>
   * <ul>
   *  <li>Location 1: 10, -2, 3</li>
   *  <li>Location 2: 0, 19, 2</li>
   * </ul>
   * this method will return 0, -2, 2
   */
  public static Location findLowestPoint(Location first, Location second) {
    if (first.getWorld() != second.getWorld()) {
      throw new IllegalArgumentException("Locations must be in the same world");
    }

    double minX = Math.min(first.getX(), second.getX());
    double minY = Math.min(first.getY(), second.getY());
    double minZ = Math.min(first.getZ(), second.getZ());

    return new Location(second.getWorld(), minX, minY, minZ);
  }

  /** Get the blocks between the starting location and the ending location. **/
  public static List<Location> getBlocksArea(Location first, Location second) {
    Location start = findLowestPoint(first, second);
    Location end = findGreatestPoint(first, second);

    ArrayList<Location> locations = new ArrayList<>();
    for (int x = start.getBlockX(); x <= end.getBlockX(); x++) {
      for (int y = start.getBlockY(); y <= end.getBlockY(); y++) {
        for (int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {
          locations.add(new Location(start.getWorld(), x, y, z));
        }
      }
    }

    return locations;
  }

}
