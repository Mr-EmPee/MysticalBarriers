package ml.empee.mysticalbarriers.model.entities;

import lombok.Getter;
import lombok.Setter;
import ml.empee.mysticalbarriers.model.content.MultiBlockPacket;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ml.empee.mysticalbarriers.utils.ObjectConverter.parseLocation;
import static ml.empee.mysticalbarriers.utils.ValidationUtils.get;

/**
 * A barrier
 */

public class Barrier {

  @Getter
  @Setter
  private String id;
  private Location lowestCorner;
  private Location greatestCorner;

  @Getter
  @Setter
  private Integer activationRange;
  @Getter
  @Setter
  private BlockData blockData;

  public Barrier(String id) {
    Objects.requireNonNull(id);

    this.id = id;
    this.blockData = Bukkit.createBlockData(Material.GLASS);
    this.activationRange = 3;
  }

  /**
   * Convert a map to a Barrier
   */
  public static Barrier fromMap(Map<String, Object> map) {
    String id = get(map, "id", String.class);
    Integer activationRange = get(map, "activation_range", Integer.class);

    if (activationRange < 1) {
      throw new IllegalArgumentException("Activation range of " + id + " can't be lower then 1");
    }

    Barrier barrier = new Barrier(id);
    barrier.setActivationRange(activationRange);

    String blockData = get(map, "block_data", String.class);
    barrier.setBlockData(Bukkit.createBlockData(blockData));

    String firstCorner = get(map, "first_corner", String.class);
    String secondCorner = get(map, "second_corner", String.class);
    barrier.setCorners(
        parseLocation(firstCorner),
        parseLocation(secondCorner)
    );

    return barrier;
  }

  /**
   * Convert the instance to a map
   */
  public Map<String, Object> toMap() {
    HashMap<String, Object> properties = new HashMap<>();
    properties.put("id", id);
    properties.put("first_corner", parseLocation(lowestCorner));
    properties.put("second_corner", parseLocation(greatestCorner));
    properties.put("block_data", blockData.getAsString());
    properties.put("activation_range", activationRange);
    properties.put("version", 2);

    return properties;
  }

  public void setCorners(Location firstCorner, Location secondCorner) {
    this.lowestCorner = Vector.getMinimum(
        firstCorner.toVector(), secondCorner.toVector()
    ).toLocation(firstCorner.getWorld());

    this.greatestCorner = Vector.getMaximum(
        firstCorner.toVector(), secondCorner.toVector()
    ).toLocation(firstCorner.getWorld());
  }

  public World getWorld() {
    return lowestCorner.getWorld();
  }

  public Location getLowestCorner() {
    return lowestCorner.clone();
  }

  public Location getGreatestCorner() {
    return greatestCorner.clone();
  }

  public boolean isNear(Location location) {
    return isNear(location, activationRange);
  }

  private boolean isNear(Location location, int range) {
    if (!getWorld().equals(location.getWorld())) {
      return false;
    }

    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();

    boolean isWithinFirstCornerX = lowestCorner.getBlockX() - range <= x;
    boolean isWithinFirstCornerY = lowestCorner.getBlockY() - range <= y;
    boolean isWithinFirstCornerZ = lowestCorner.getBlockZ() - range <= z;

    boolean isWithinSecondCornerX = greatestCorner.getBlockX() + range >= x;
    boolean isWithinSecondCornerY = greatestCorner.getBlockY() + range >= y;
    boolean isWithinSecondCornerZ = greatestCorner.getBlockZ() + range >= z;

    return isWithinSecondCornerX
        && isWithinFirstCornerX
        && isWithinFirstCornerY
        && isWithinSecondCornerY
        && isWithinFirstCornerZ
        && isWithinSecondCornerZ;
  }

  public boolean isBarrierAt(Location location) {
    return isNear(location, 0);
  }

  public boolean isBarrierBlock(Block block) {
    if (!isBarrierAt(block.getLocation())) {
      return false;
    }

    return block.getType().name().contains("AIR");
  }

  /**
   * Show the barrier to all the players within range
   */
  public void showBarrier() {
    for (Player player : getWorld().getPlayers()) {
      if (isVisibleFor(player)) {
        showBarrier(player, player.getLocation());
      }
    }
  }

  /**
   * Send the barrier blocks within the barrier range to the player
   */
  public void showBarrier(Player player, Location location) {
    sendBarrierBlocksTo(player, location, blockData);
  }

  private void sendBarrierBlocksTo(Player player, Location location, BlockData block) {
    MultiBlockPacket packet = new MultiBlockPacket(false);
    LocationUtils.getBlocksWithin(location, activationRange).forEach(
        loc -> {
          if (!isBarrierBlock(loc.getBlock())) {
            return;
          }

          packet.addBlock(loc, block);
        }
    );

    packet.send(player);
  }

  /**
   * Hide the barrier blocks within the barrier range to the player
   */
  public void hideBarrier(Player player, Location location) {
    sendBarrierBlocksTo(player, location, Bukkit.createBlockData(Material.AIR));
  }

  /**
   * Hide the barrier to all the players
   */
  public void hideBarrier() {
    for (Player player : getWorld().getPlayers()) {
      if (isVisibleFor(player)) {
        hideBarrier(player, player.getLocation());
      }
    }
  }

  public boolean isVisibleFor(Player player) {
    //TODO: Change
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Barrier target) {
      return target.getId().equals(id);
    }

    return false;
  }
}
