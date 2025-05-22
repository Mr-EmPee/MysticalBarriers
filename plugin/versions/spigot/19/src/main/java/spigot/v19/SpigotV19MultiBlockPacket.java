package spigot.v19;

import common.IMultiBlockPacket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpigotV19MultiBlockPacket implements IMultiBlockPacket {

  private final List<BlockState> blocks = new ArrayList<>();

  public void addBlock(Material type, Location location) {
    BlockState state = location.getBlock().getState();
    state.setType(type);
    blocks.add(state);
  }

  public void addBlock(BlockData blockData, Location location) {
    BlockState state = location.getBlock().getState();
    state.setBlockData(blockData);
    blocks.add(state);}

  public void send(Player... players) {
    for (Player player : players) {
      player.sendBlockChanges(blocks, false);
    }
  }

}
