package ml.empee.mysticalbarriers.model.entities;

import lombok.Data;
import ml.empee.mysticalbarriers.Permissions;
import ml.empee.mysticalbarriers.model.dto.BarrierDTO;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static ml.empee.mysticalbarriers.utils.JsonSerializationUtils.parseLocation;

@Data
public class Barrier {

  private String id;
  private Location firstCorner;
  private Location secondCorner;
  private Material material;
  private Integer activationRange;
  private BlockData blockData;

  public Barrier(String id) {
    this.id = id;
    this.material = Material.BARRIER;
    this.activationRange = 3;
  }
  public Barrier(BarrierDTO dto) {
    id = dto.getId();

    setCorners(parseLocation(dto.getFirstCorner()), parseLocation(dto.getSecondCorner()));
    activationRange = dto.getActivationRange();
    if(activationRange < 1) {
      throw new IllegalArgumentException("Activation range of " + dto.getId() + " can't be lower then 1");
    }

    try {
      material = Material.valueOf(dto.getMaterial());
      if(dto.getBlockData() != null) {
        blockData = Bukkit.createBlockData(dto.getBlockData());
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
        "The block '" + (dto.getBlockData() == null ? dto.getMaterial() : dto.getBlockData())
          + "' of the barrier '" + dto.getId() + "' is " + "invalid!"
      );
    }
  }
  public BarrierDTO toDTO() {
    return BarrierDTO.builder()
      .id(id)
      .firstCorner(parseLocation(firstCorner))
      .secondCorner(parseLocation(secondCorner))
      .material(material.name())
      .activationRange(activationRange)
      .blockData(blockData == null ? null : blockData.getAsString())
      .version(1)
      .build();
  }

  public void setFirstCorner(Location firstCorner) {
    setCorners(firstCorner, secondCorner);
  }
  public void setSecondCorner(Location secondCorner) {
    setCorners(firstCorner, secondCorner);
  }
  public void setCorners(Location firstCorner, Location secondCorner) {
    this.firstCorner = LocationUtils.findLowestPoint(firstCorner, secondCorner);
    this.secondCorner = LocationUtils.findGreatestPoint(firstCorner, secondCorner);
  }

  public boolean isBarrierAt(@Nullable World world, int x, int y, int z) {
    if (world != null && !getWorld().equals(world)) {
      return false;
    }

    return x >= firstCorner.getBlockX()
      && x <= secondCorner.getBlockX()
      && y >= firstCorner.getBlockY()
      && y <= secondCorner.getBlockY()
      && z >= firstCorner.getBlockZ()
      && z <= secondCorner.getBlockZ();
  }
  public boolean isBarrierAt(Location location) {
    return isBarrierAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  /**
   * @param world if not null checks even the world
   * @param range if null use the barrier activation range
   */
  public boolean isWithinRange(@Nullable World world, @Nullable Integer range, int x, int y, int z) {
    if (world != null && !getWorld().equals(world)) {
      return false;
    }

    if (range == null) {
      range = activationRange;
    }

    boolean isWithinFirstCornerX = firstCorner.getBlockX() - range <= x;
    boolean isWithinFirstCornerY = firstCorner.getBlockY() - range <= y;
    boolean isWithinFirstCornerZ = firstCorner.getBlockZ() - range <= z;

    boolean isWithinSecondCornerX = secondCorner.getBlockX() + range >= x;
    boolean isWithinSecondCornerY = secondCorner.getBlockY() + range >= y;
    boolean isWithinSecondCornerZ = secondCorner.getBlockZ() + range >= z;

    return isWithinSecondCornerX
      && isWithinFirstCornerX
      && isWithinFirstCornerY
      && isWithinSecondCornerY
      && isWithinFirstCornerZ
      && isWithinSecondCornerZ;
  }
  public boolean isWithinRange(Location location, @Nullable Integer range) {
    return isWithinRange(
      location.getWorld(), range, location.getBlockX(), location.getBlockY(), location.getBlockZ()
    );
  }

  public boolean isHiddenFor(Player player) {
    return player.hasPermission(Permissions.BYPASS_PERMISSION + id);
  }
  public void forEachVisibleBarrierBlock(Location location, Consumer<Location> consumer) {
    if (!getWorld().equals(location.getWorld())) {
      return;
    }

    World world = location.getWorld();
    LocationUtils.radiusSearch(location, activationRange, loc -> {
      int x = loc.getBlockX();
      int y = loc.getBlockY();
      int z = loc.getBlockZ();

      if (world.getBlockAt(x, y, z).getType() == Material.AIR) {
        if (isBarrierAt(null, x, y, z)) {
          consumer.accept(loc);
        }
      }
    });
  }
  public World getWorld() {
    return firstCorner.getWorld();
  }
}
