package ml.empee.mysticalbarriers.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationUtils {

  public static boolean hasChangedBlock(Location fromLoc, Location toLoc) {
    if (toLoc == null) {
      return false;
    }

    toLoc = toLoc.getBlock().getLocation();
    fromLoc = fromLoc.getBlock().getLocation();
    if (toLoc.equals(fromLoc)) {
      return false;
    }

    return true;
  }

  public static void radiusSearch(Location location, int radius, Consumer<Location> consumer) {
    int maxX = location.getBlockX() + radius;
    int maxY = location.getBlockY() + radius;
    int maxZ = location.getBlockZ() + radius;
    int minX = location.getBlockX() - radius;
    int minY = location.getBlockY() - radius;
    int minZ = location.getBlockZ() - radius;

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        for (int z = minZ; z <= maxZ; z++) {
          consumer.accept(new Location(location.getWorld(), x, y, z));
        }
      }
    }
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
  public static int getGreatestAxisDistance(Location location1, Location location2) {
    int x = location1.getBlockX() - location2.getBlockX();
    int y = location1.getBlockY() - location2.getBlockY();
    int z = location1.getBlockZ() - location2.getBlockZ();

    return Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
  }

  public static Stream<Location> forEachAdjacentBlock(Location center) {
    center = center.getBlock().getLocation();

    return Stream.of(
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

  public static List<Location> getBlocksBetween(Location start, Location end) {
    Location min = findLowestPoint(start, end);
    Location max = findGreatestPoint(start, end);

    ArrayList<Location> locations = new ArrayList<>();
    for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
      for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
          locations.add(new Location(start.getWorld(), x, y, z));
        }
      }
    }

    return locations;
  }

  public static List<Location> getBlocksBetween(Location start, Vector velocity) {
    return getBlocksBetween(start, start.clone().add(velocity));
  }

}
