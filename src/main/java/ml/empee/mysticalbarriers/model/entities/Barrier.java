package ml.empee.mysticalbarriers.model.entities;

import lombok.Getter;
import lombok.Setter;
import ml.empee.mysticalbarriers.Permissions;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import ml.empee.mysticalbarriers.utils.ValidationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static ml.empee.mysticalbarriers.utils.JsonSerializationUtils.parseLocation;

@Getter
public class Barrier {

  @Setter
  private String id;
  private Location firstCorner;
  private Location secondCorner;

  @Setter
  private Material material;
  @Setter
  private Integer activationRange;
  @Setter
  private BlockData blockData;

  public Barrier(String id) {
    Objects.requireNonNull(id);

    this.id = id;
    this.material = Material.BARRIER;
    this.activationRange = 3;
  }

  public static Barrier fromMap(Map<String, Object> map) {
    String id = ValidationUtils.has(map, "id", String.class);
    String firstCorner = ValidationUtils.has(map, "first_corner", String.class);
    String secondCorner = ValidationUtils.has(map, "second_corner", String.class);
    String material = ValidationUtils.has(map, "material", String.class);
    Integer activationRange = ValidationUtils.has(map, "activation_range", Integer.class);
    Optional<String> blockData = ValidationUtils.hasType(map, "block_data", String.class);

    if (activationRange < 1) {
      throw new IllegalArgumentException("Activation range of " + id + " can't be lower then 1");
    }

    Barrier barrier = new Barrier(id);
    barrier.setMaterial(Material.valueOf(material));
    barrier.setActivationRange(activationRange);
    blockData.ifPresent(s -> barrier.setBlockData(Bukkit.createBlockData(s)));
    barrier.setCorners(
      parseLocation(firstCorner),
      parseLocation(secondCorner)
    );

    return barrier;
  }
  public Map<String, Object> toMap() {
    HashMap<String, Object> properties = new HashMap<>();
    properties.put("id", id);
    properties.put("first_corner", parseLocation(firstCorner));
    properties.put("second_corner", parseLocation(secondCorner));
    properties.put("material", material.name());
    properties.put("activation_range", activationRange);
    properties.put("version", 1);
    if (blockData != null) {
      properties.put("block_data", blockData.getAsString());
    }

    return properties;
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
