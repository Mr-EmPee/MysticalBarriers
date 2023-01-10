package ml.empee.mysticalBarriers.controllers.parsers;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Wall;

@RequiredArgsConstructor
public enum BarrierDirection {

  EAST_WEST("[east=true,west=true,north=false,south=false]"),
  NORTH_SOUTH("[north=true,south=true,east=false,west=false]"),
  ALL("[east=true,west=true,north=true,south=true]"),
  NONE("[east=false,west=false,north=false,south=false]");

  private final String data;

  public String buildFacesData(Material material) {
    BlockData data = material.createBlockData();
    if (!(data instanceof MultipleFacing) && !(data instanceof Wall)) {
      return null;
    }

    if (data instanceof MultipleFacing) {
      return this.data;
    } else {
      return this.data.replace("true", "tall").replace("false", "none");
    }
  }

}
