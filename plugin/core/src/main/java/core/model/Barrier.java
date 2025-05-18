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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter @Setter
public class Barrier {

  public static final Material STRUCTURE_MASK = Material.BEDROCK;
  public static final BlockData STRUCTURE_DEFAULT_BLOCK = Material.AIR.createBlockData();
  public static final BlockData DEFAULT_FILL_BLOCK = Material.BARRIER.createBlockData();

  @Id
  private String id;
  private CubicRegion region;

  @Builder.Default
  private BlockData fillBlock = DEFAULT_FILL_BLOCK;
  private List<Block> structure;

  private transient Map<Location, Block> structureCache;

  @Builder.Default
  private int activationRange = 3;

  @Nullable
  public BlockData getBlockDataAt(Location location) {
    if (structure == null) {
      return fillBlock;
    }

    if (structureCache == null) {
      structureCache = new HashMap<>();
      structure.forEach(b -> structureCache.put(b.getLocation(), b));
    }

    Block result = structureCache.get(location);
    if (result != null) {
      return result.getData();
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
