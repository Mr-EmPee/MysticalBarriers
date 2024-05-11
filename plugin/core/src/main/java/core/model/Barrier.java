package core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.dizitart.no2.repository.annotations.Id;
import org.jetbrains.annotations.Nullable;
import utils.regions.CubicRegion;

import java.util.List;

@Builder
@Getter @Setter
public class Barrier {

  public static final Material MASK_BLOCK = Material.BEDROCK;
  public static final BlockData DEFAULT_BLOCK = Material.AIR.createBlockData();

  @Id
  private String id;
  private CubicRegion region;

  private List<Block> barrierBlocks;

  @Builder.Default
  private int activationRange = 3;

  @Nullable
  public BlockData getBlockAt(Location location) {
    for (Block block : barrierBlocks) {
      if (location.equals(block.getLocation())) {
        return block.getData();
      }
    }

    return null;
  }

  public boolean isWithin(Location location) {
    if (!region.getWorld().equals(location.getWorld())) {
      return false;
    }

    var min = region.getMin();
    var max = region.getMax();

    return location.getX() >= min.getBlockX()
        && location.getX() <= max.getBlockX()
        && location.getY() >= min.getBlockY()
        && location.getY() <= max.getBlockY()
        && location.getZ() >= min.getBlockZ()
        && location.getZ() <= max.getBlockZ();
  }

  @Nullable
  public CubicRegion findIntersection(CubicRegion perimeter) {
    var barrierMin = getRegion().getMin();
    var barrierMax = getRegion().getMax();

    var perimeterMin = perimeter.getMin();
    var perimeterMax = perimeter.getMax();

    int minX = Math.max(barrierMin.getBlockX(), perimeterMin.getBlockX());
    int minY = Math.max(barrierMin.getBlockY(), perimeterMin.getBlockY());
    int minZ = Math.max(barrierMin.getBlockZ(), perimeterMin.getBlockZ());

    int maxX = Math.min(barrierMax.getBlockX(), perimeterMax.getBlockX());
    int maxY = Math.min(barrierMax.getBlockY(), perimeterMax.getBlockY());
    int maxZ = Math.min(barrierMax.getBlockZ(), perimeterMax.getBlockZ());

    if (!(minX <= maxX && minY <= maxY && minZ <= maxZ)) {
      return null;
    }

    return CubicRegion.of(
        new Location(getRegion().getWorld(), minX, minY, minZ),
        new Location(getRegion().getWorld(), maxX, maxY, maxZ)
    );
  }

  @Getter @Setter
  @AllArgsConstructor(staticName = "of")
  public static class Block {
    private Location location;
    private BlockData data;
  }

}
