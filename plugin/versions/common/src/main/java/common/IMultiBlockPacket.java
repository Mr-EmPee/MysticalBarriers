package common;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public interface IMultiBlockPacket {

  void addBlock(Material type, Location location);

  void addBlock(BlockData blockData, Location location);

  void send(Player... players);

}
