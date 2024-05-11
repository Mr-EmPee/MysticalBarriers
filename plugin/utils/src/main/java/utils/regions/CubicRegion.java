package utils.regions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class CubicRegion {

  private final Location[] corners = new Location[2];
  private transient int currentPoint;

  public static CubicRegion of(Location first, Location second) {
    var result = new CubicRegion();
    result.add(first);
    result.add(second);
    return result;
  }

  public static CubicRegion of(Location center, double margin) {
    var first = center.clone().subtract(margin, margin, margin);
    var second = center.clone().add(margin, margin, margin);

    return of(first, second);
  }

  public Location getFirst() {
    return corners[0];
  }

  public Location getSecond() {
    return corners[1];
  }

  public int add(Location location) {
    corners[currentPoint % 2] = location.clone();
    return currentPoint++;
  }

  public boolean isValid() {
    if (corners[0] == null || corners[1] == null) {
      return false;
    }

    if (!corners[0].getWorld().equals(corners[1].getWorld())) {
      return false;
    }

    return true;
  }

  public World getWorld() {
    return corners[0].getWorld();
  }

  public Location getMax() {
    var max = Vector.getMaximum(corners[0].toVector(), corners[1].toVector());
    return new Location(getWorld(), max.getX(), max.getY(), max.getZ());
  }

  public Location getMin() {
    var min = Vector.getMinimum(corners[0].toVector(), corners[1].toVector());
    return new Location(getWorld(), min.getX(), min.getY(), min.getZ());
  }

  public void forEach(Consumer<Location> action) {
    var min = getMin();
    var max = getMax();
    var world = getWorld();

    for (double x = min.getX(); x<=max.getX(); x++) {
      for (double y = min.getY(); y<=max.getY(); y++) {
        for (double z = min.getZ(); z<=max.getZ(); z++) {
          action.accept(new Location(world, x, y, z));
        }
      }
    }
  }

}
