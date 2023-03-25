package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonSerializationUtils {

  public static Location parseLocation(String rawLoc) {
    if (rawLoc == null || !rawLoc.matches(".+:-?\\d+:-?\\d+:-?\\d+")) {
      throw new RuntimeException("The location '" + rawLoc + "' must match '.+:-?\\d+:-?\\d+:-?\\d+'");
    }

    String[] coordinates = rawLoc.split(":");
    World world = Bukkit.getWorld(coordinates[0]);
    if(world == null) {
      throw new RuntimeException("Unable to find world " + coordinates[0]);
    }

    return new Location(world,
      Integer.parseInt(coordinates[1]),
      Integer.parseInt(coordinates[2]),
      Integer.parseInt(coordinates[3])
    );
  }
  public static String parseLocation(Location loc) {
    return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
  }
  public static LocalDateTime parseTime(String rawTime) {
    return LocalDateTime.parse(rawTime);
  }
  public static String parseTime(LocalDateTime time) {
    return time.toString();
  }

}
