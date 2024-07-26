package utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public class LocationUtils {

  public Location toBlockLocation(Location location) {
    Location blockLoc = location.clone();
    blockLoc.setX(blockLoc.getBlockX());
    blockLoc.setY(blockLoc.getBlockY());
    blockLoc.setZ(blockLoc.getBlockZ());
    return blockLoc;
  }

}
