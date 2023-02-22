package ml.empee.mysticalbarriers.controllers.parsers;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@RequiredArgsConstructor
public enum BarrierDirection {

  EAST_WEST("[east=true,west=true,north=false,south=false]"),
  NORTH_SOUTH("[north=true,south=true,east=false,west=false]"),
  ALL("[east=true,west=true,north=true,south=true]"),
  NONE("[east=false,west=false,north=false,south=false]");

  private final String data;

  public String buildFacesData(Material material) {
    try {
      material.createBlockData(data);
      return data;
    } catch (IllegalArgumentException ignore) {
      // ignore
    }

    try {
      String wallData = data.replace("true", "tall").replace("false", "none");
      material.createBlockData(wallData);
      return wallData;
    } catch (IllegalArgumentException ignore) {
      return null;
    }
  }

}
