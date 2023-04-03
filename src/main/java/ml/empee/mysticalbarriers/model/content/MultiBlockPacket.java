package ml.empee.mysticalbarriers.model.content;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.primitives.Shorts;
import lombok.SneakyThrows;
import ml.empee.mysticalbarriers.utils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for a multi-block packet
 */

public class MultiBlockPacket {
  private static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
  private static final boolean IS_AFTER_1_16_2 = ServerVersion.isGreaterThan(1, 16, 2);

  private final PacketContainer packet = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
  private final Map<Location, WrappedBlockData> blocks = new LinkedHashMap<>();
  private final boolean suppressLightUpdates;
  private Integer packetChunkX;
  private Integer packetChunkY;
  private Integer packetChunkZ;
  private MultiBlockPacket subPacket;

  public MultiBlockPacket(boolean suppressLightUpdates) {
    this.suppressLightUpdates = suppressLightUpdates;
  }

  /**
   * Add a block to the packet
   */
  public void addBlock(Location location, BlockData block) {
    int blockChunkX = location.getBlockX() >> 4;
    int blockChunkY = location.getBlockY() >> 4;
    int blockChunkZ = location.getBlockZ() >> 4;

    if (packetChunkX == null || packetChunkY == null || packetChunkZ == null) {
      packetChunkX = blockChunkX;
      packetChunkY = blockChunkY;
      packetChunkZ = blockChunkZ;
    } else {
      boolean isOutsidePacketX = this.packetChunkX != blockChunkX;
      boolean isOutsidePacketY = this.packetChunkY != blockChunkY;
      boolean isOutsidePacketZ = this.packetChunkZ != blockChunkZ;
      if (isOutsidePacketX || isOutsidePacketY || isOutsidePacketZ) {
        if (subPacket == null) {
          subPacket = new MultiBlockPacket(suppressLightUpdates);
        }

        subPacket.addBlock(location, block);
        return;
      }
    }

    blocks.put(location, WrappedBlockData.createData(block));
  }

  /**
   * Send the multi-block packet
   */
  @SneakyThrows
  public void send(Player... players) {
    if (!blocks.isEmpty()) {
      if (IS_AFTER_1_16_2) {
        writePacketAfter1_16_2();
      } else {
        writePacketBefore1_16_2();
      }

      for (Player player : players) {
        PROTOCOL_MANAGER.sendServerPacket(player, packet);
      }
    }

    if (subPacket != null) {
      subPacket.send(players);
    }
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

  private void writePacketAfter1_16_2() {
    packet.getSectionPositions().write(0, new BlockPosition(packetChunkX, packetChunkY, packetChunkZ));
    packet.getBooleans().writeSafely(0, suppressLightUpdates);

    packet.getBlockDataArrays().write(0, blocks.values().toArray(new WrappedBlockData[0]));
    packet.getShortArrays().write(0, Shorts.toArray(
        blocks.keySet().stream()
            .map(l -> getRelativePosition(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            .toList()
    ));
  }

  private void writePacketBefore1_16_2() {
    packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(packetChunkX, packetChunkZ));
    MultiBlockChangeInfo[] multiBlockChangeInfo = new MultiBlockChangeInfo[blocks.size()];

    int i = 0;
    for (var entry : blocks.entrySet()) {
      multiBlockChangeInfo[i] = new MultiBlockChangeInfo(entry.getKey(), entry.getValue());
      i++;
    }

    packet.getMultiBlockChangeInfoArrays().write(0, multiBlockChangeInfo);
  }

}
