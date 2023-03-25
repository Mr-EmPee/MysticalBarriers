package ml.empee.mysticalbarriers.controllers.parsers;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

@RequiredArgsConstructor
public enum BarrierDirection {

  EAST_WEST("[east=true,west=true,north=false,south=false]"),
  NORTH_SOUTH("[north=true,south=true,east=false,west=false]"),
  ALL("[east=true,west=true,north=true,south=true]"),
  NONE("[east=false,west=false,north=false,south=false]");

  private final String data;

  public BlockData buildFacesData(Material material) {
    try {
      return material.createBlockData(data);
    } catch (IllegalArgumentException ignore) {
      // ignore
    }

    try {
      String wallData = data.replace("true", "tall").replace("false", "none");
      return material.createBlockData(wallData);
    } catch (IllegalArgumentException ignore) {
      return null;
    }
  }

}
