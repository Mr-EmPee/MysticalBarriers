package core.packets;

import common.IMultiBlockPacket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import protocolib.ProtocolLibMultiBlockPacket;
import spigot.v19.SpigotV19MultiBlockPacket;
import utils.ServerVersion;

public class MultiBlockPacket implements IMultiBlockPacket {

  private final IMultiBlockPacket provider;

  public MultiBlockPacket() {
    if (ServerVersion.isGreaterThan(1, 19, 4)) {
      provider = new SpigotV19MultiBlockPacket();
    } else {
      provider = new ProtocolLibMultiBlockPacket();
    }
  }

  public void addBlock(Material type, Location location) {
    provider.addBlock(type, location);
  }

  public void addBlock(BlockData blockData, Location location) {
    provider.addBlock(blockData, location);
  }

  public void send(Player... players) {
    provider.send(players);
  }

}