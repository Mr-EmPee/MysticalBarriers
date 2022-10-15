package ml.empee.mysticalBarriers.utils;

import org.bukkit.Location;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.mysticalBarriers.helpers.TriConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationUtils {

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

  public static int fastBlockDistance(Location location1, Location location2) {
    int x = location1.getBlockX() - location2.getBlockX();
    int y = location1.getBlockY() - location2.getBlockY();
    int z = location1.getBlockZ() - location2.getBlockZ();

    return Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
  }

}
