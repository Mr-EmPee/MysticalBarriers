package ml.empee.mysticalbarriers.model.entities;

import lombok.Getter;
import lombok.Setter;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static ml.empee.mysticalbarriers.utils.ObjectConverter.parseLocation;
import static ml.empee.mysticalbarriers.utils.ValidationUtils.get;
import static ml.empee.mysticalbarriers.utils.ValidationUtils.getOrEmpty;

/**
 * A barrier
 */

public class Barrier {

  @Getter @Setter
  private String id;
  private Location firstCorner;
  private Location secondCorner;

  @Getter @Setter
  private Material material;
  @Getter @Setter
  private Integer activationRange;
  @Getter @Setter
  private BlockData blockData;

  public Barrier(String id) {
    Objects.requireNonNull(id);

    this.id = id;
    this.material = Material.BARRIER;
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

    String material = get(map, "material", String.class);
    barrier.setMaterial(Material.valueOf(material));

    Optional<String> blockData = getOrEmpty(map, "block_data", String.class);
    blockData.ifPresent(s -> barrier.setBlockData(Bukkit.createBlockData(s)));

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
    properties.put("first_corner", parseLocation(firstCorner));
    properties.put("second_corner", parseLocation(secondCorner));
    properties.put("material", material.name());
    properties.put("activation_range", activationRange);
    properties.put("version", 2);
    if (blockData != null) {
      properties.put("block_data", blockData.getAsString());
    }

    return properties;
  }

  public void setCorners(Location firstCorner, Location secondCorner) {
    this.firstCorner = LocationUtils.findLowestPoint(firstCorner, secondCorner);
    this.secondCorner = LocationUtils.findGreatestPoint(firstCorner, secondCorner);
  }

  public Location getFirstCorner() {
    return firstCorner.clone();
  }

  public Location getSecondCorner() {
    return secondCorner.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Barrier target) {
      return target.getId().equals(id);
    }

    return false;
  }
}
