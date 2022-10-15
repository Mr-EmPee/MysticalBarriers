package ml.empee.mysticalBarriers.utils;

import org.bukkit.Location;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.mysticalBarriers.helpers.TriConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationUtils {

  public static void radiusSearch(Location location, int radius, TriConsumer<Integer, Integer, Integer> consumer) {
    for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
      for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
        for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
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
