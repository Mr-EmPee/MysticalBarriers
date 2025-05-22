package protocolib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import common.IMultiBlockPacket;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import utils.ServerVersion;

import java.util.ArrayList;

public class ProtocolLibMultiBlockPacket implements IMultiBlockPacket {
  private static final PacketType type = PacketType.Play.Server.MULTI_BLOCK_CHANGE;

  private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private final PacketContainer packet = new PacketContainer(type);

  private int packetChunkX;
  private int packetChunkY;
  private int packetChunkZ;

  private final ArrayList<Location> blocksLocations = new ArrayList<>();
  private final ArrayList<WrappedBlockData> blocksData = new ArrayList<>();
  private ProtocolLibMultiBlockPacket subPacket;

  private void addBlock(Material type, Integer subId, Object spigotBlockData, Location location) {
    int blockChunkX = location.getBlockX() >> 4;
    int blockChunkY = location.getBlockY() >> 4;
    int blockChunkZ = location.getBlockZ() >> 4;

    if (blocksLocations.isEmpty()) {
      this.packetChunkX = blockChunkX;
      this.packetChunkY = blockChunkY;
      this.packetChunkZ = blockChunkZ;
    }

    boolean isOutsidePacketX = this.packetChunkX != blockChunkX;
    boolean isOutsidePacketY = this.packetChunkY != blockChunkY;
    boolean isOutsidePacketZ = this.packetChunkZ != blockChunkZ;
    if (isOutsidePacketX || isOutsidePacketY || isOutsidePacketZ) {
      if (subPacket == null) {
        subPacket = new ProtocolLibMultiBlockPacket();
      }

      subPacket.addBlock(type, subId, spigotBlockData, location);
    } else {
      if (subId == null) {
        if (spigotBlockData == null) {
          blocksData.add(WrappedBlockData.createData(type));
        } else {
          blocksData.add(WrappedBlockData.createData(spigotBlockData));
        }

      } else {
        blocksData.add(WrappedBlockData.createData(type, subId));
      }

      blocksLocations.add(location);
    }

  }

  public void addBlock(Material type, Location location) {
    addBlock(type, null, null, location);
  }

  public void addBlock(BlockData blockData, Location location) {
    addBlock(null, null, blockData, location);
  }

  /**
   * Gets the relative position of a block relative to the chunk, encoded inside a short.
   */
  private short getRelativePosition(int x, int y, int z) {
    //Convert to location within chunk.
    x = x & 0xF;
    y = y & 0xF;
    z = z & 0xF;

    //Creates position from location within chunk
    return (short) (x << 8 | z << 4 | y);
  }

  @SneakyThrows
  public void send(Player... players) {
    if (blocksData.isEmpty() && subPacket == null) {
      return;
    }

    if (ServerVersion.isGreaterThan(1, 16, 2)) {
      writePacketAfter1_16_2();
    } else {
      writePacketBefore1_16_2();
    }

    for (Player player : players) {
      protocolManager.sendServerPacket(player, packet);
    }

    if (subPacket != null) {
      subPacket.send(players);
    }
  }

  private void writePacketAfter1_16_2() {
    WrappedBlockData[] blocksDataArray = blocksData.toArray(new WrappedBlockData[0]);
    short[] blocksLocationsArray = new short[blocksLocations.size()];
    for (int i = 0; i < blocksLocationsArray.length; i++) {
      Location location = blocksLocations.get(i);
      blocksLocationsArray[i] = getRelativePosition(
          location.getBlockX(), location.getBlockY(), location.getBlockZ()
      );
    }

    packet.getSectionPositions().write(0, new BlockPosition(packetChunkX, packetChunkY, packetChunkZ));
    packet.getBooleans().writeSafely(0, false);

    packet.getBlockDataArrays().write(0, blocksDataArray);
    packet.getShortArrays().write(0, blocksLocationsArray);
  }

  private void writePacketBefore1_16_2() {
    packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(packetChunkX, packetChunkZ));
    MultiBlockChangeInfo[] multiBlockChangeInfo = new MultiBlockChangeInfo[blocksData.size()];
    for (int i = 0; i < blocksData.size(); i++) {
      multiBlockChangeInfo[i] = new MultiBlockChangeInfo(blocksLocations.get(i), blocksData.get(i));
    }

    packet.getMultiBlockChangeInfoArrays().write(0, multiBlockChangeInfo);
  }

}
