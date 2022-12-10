package ml.empee.mysticalBarriers.utils;

import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.mysticalBarriers.helpers.TriConsumer;
import org.bukkit.Location;

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

  public static void radiusSearch(Location location, int radius, TriConsumer<Integer, Integer, Integer> consumer) {
    int maxX = location.getBlockX() + radius;
    int maxY = location.getBlockY() + radius;
    int maxZ = location.getBlockZ() + radius;
    int minX = location.getBlockX() - radius;
    int minY = location.getBlockY() - radius;
    int minZ = location.getBlockZ() - radius;

    for(int y = minY; y<=maxY; y++) {
      for(int x = minX; x <= maxX; x++) {
        for (int z = minZ; z <= maxZ; z++) {
          consumer.accept(x, y, z);
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

}
