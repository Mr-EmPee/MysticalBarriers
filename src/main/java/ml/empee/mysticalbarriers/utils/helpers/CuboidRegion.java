package ml.empee.mysticalbarriers.utils.helpers;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * A cuboid region
 */

@NoArgsConstructor(staticName = "empty")
public class CuboidRegion {
  private Location firstCorner;
  private Location secondCorner;

  private Location lowestCorner;
  private Location greatestCorner;

  public static CuboidRegion of(Location firstCorner, Location secondCorner) {
    CuboidRegion result = new CuboidRegion();
    result.setCorners(firstCorner, secondCorner);
    return result;
  }

  /**
   * @return false if a corner is null or the corners' worlds aren't the same
   */
  public boolean isValid() {
    return firstCorner != null && secondCorner != null && firstCorner.getWorld().equals(secondCorner.getWorld());
  }

  /**
   * Remove the region corners
   */
  public void invalidate() {
    firstCorner = null;
    secondCorner = null;
    lowestCorner = null;
    greatestCorner = null;
  }

  private void ordinate() {
    this.lowestCorner = Vector.getMinimum(
        firstCorner.toVector(), secondCorner.toVector()
    ).toLocation(getWorld());

    this.greatestCorner = Vector.getMaximum(
        firstCorner.toVector(), secondCorner.toVector()
    ).toLocation(getWorld());
  }

  /**
   * @return true if the provided location is at most X blocks away
   */
  public boolean isNear(Location location, int maxDistance) {
    if (!getWorld().equals(location.getWorld())) {
      return false;
    }

    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();

    boolean isWithinFirstCornerX = lowestCorner.getBlockX() - maxDistance <= x;
    boolean isWithinFirstCornerY = lowestCorner.getBlockY() - maxDistance <= y;
    boolean isWithinFirstCornerZ = lowestCorner.getBlockZ() - maxDistance <= z;

    boolean isWithinSecondCornerX = greatestCorner.getBlockX() + maxDistance >= x;
    boolean isWithinSecondCornerY = greatestCorner.getBlockY() + maxDistance >= y;
    boolean isWithinSecondCornerZ = greatestCorner.getBlockZ() + maxDistance >= z;

    return isWithinSecondCornerX
        && isWithinFirstCornerX
        && isWithinFirstCornerY
        && isWithinSecondCornerY
        && isWithinFirstCornerZ
        && isWithinSecondCornerZ;
  }

  public boolean isRegionAt(Location location) {
    return isNear(location, 0);
  }

  public void setCorners(@NonNull Location firstCorner, @NonNull Location secondCorner) {
    this.firstCorner = firstCorner;
    this.secondCorner = secondCorner;

    ordinate();
  }

  public void setFirstCorner(@NonNull Location firstCorner) {
    this.firstCorner = firstCorner;

    if (secondCorner != null) {
      ordinate();
    }
  }

  public void setSecondCorner(@NonNull Location secondCorner) {
    this.secondCorner = secondCorner;

    if (firstCorner != null) {
      ordinate();
    }
  }

  public Location getFirstCorner() {
    return firstCorner.clone();
  }

  public Location getSecondCorner() {
    return secondCorner.clone();
  }

  public Location getLowestCorner() {
    return lowestCorner.clone();
  }

  public Location getGreatestCorner() {
    return greatestCorner.clone();
  }

  public World getWorld() {
    return firstCorner.getWorld();
  }

  public CuboidRegion clone() {
    return CuboidRegion.of(lowestCorner, greatestCorner);
  }

}
