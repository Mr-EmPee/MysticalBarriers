package ml.empee.mysticalBarriers.model.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import ml.empee.mysticalBarriers.utils.nms.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class MultiBlockPacket {

  private static final boolean IS_ABOVE_1_13 = ServerVersion.isGreaterThan(1, 13);
  private static final PacketType type = PacketType.Play.Server.MULTI_BLOCK_CHANGE;

  private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private final PacketContainer packet = new PacketContainer(type);

  private final boolean suppressLightUpdates;
  private final int packetChunkX;
  private final int packetChunkY;
  private final int packetChunkZ;

  private final ArrayList<Location> blocksLocations = new ArrayList<>();
  private final ArrayList<WrappedBlockData> blocksData = new ArrayList<>();
  private MultiBlockPacket subPacket;

  public MultiBlockPacket(Location center, boolean suppressLightUpdates) {
    this.suppressLightUpdates = suppressLightUpdates;

    //A chunk is 16x16, that's why you can divide by 16 to get the chunk coordinates. Because 16 is a power of 2, we can just shift the bits by 4
    packetChunkX = center.getBlockX() >> 4;
    packetChunkY = center.getBlockY() >> 4;
    packetChunkZ = center.getBlockZ() >> 4;

  }

  private MultiBlockPacket(int packetChunkX, int packetChunkY, int packetChunkZ, boolean suppressLightUpdates) {
    this.packetChunkX = packetChunkX;
    this.packetChunkY = packetChunkY;
    this.packetChunkZ = packetChunkZ;
    this.suppressLightUpdates = suppressLightUpdates;
  }

  private void addBlock(Material type, Integer blockDataValue, Object spigotBlockData, Location location) {

    int blockChunkX = location.getBlockX() >> 4;
    int blockChunkY = location.getBlockY() >> 4;
    int blockChunkZ = location.getBlockZ() >> 4;

    if (this.packetChunkX != blockChunkX || this.packetChunkY != blockChunkY || this.packetChunkZ != blockChunkZ) {
      if (subPacket == null) {
        subPacket = new MultiBlockPacket(blockChunkX, blockChunkY, blockChunkZ, suppressLightUpdates);
      }

      subPacket.addBlock(type, blockDataValue, spigotBlockData, location);
    } else {
      if (blockDataValue == null) {
        if (spigotBlockData == null) {
          blocksData.add(WrappedBlockData.createData(type));
        } else {
          blocksData.add(WrappedBlockData.createData(spigotBlockData));
        }

      } else {
        blocksData.add(WrappedBlockData.createData(type, blockDataValue));
      }

      blocksLocations.add(location);
    }

  }

  public void addBlock(Material type, @Nullable Integer blockSubCategory, Location location) {
    addBlock(type, blockSubCategory, null, location);
  }

  public void addBlock(Material type, Location location) {
    addBlock(type, null, null, location);
  }

  public void addBlock(BlockData blockData, Location location) {
    addBlock(null, null, blockData, location);
  }

  public void addBlock(Material type, int x, int y, int z) {
    addBlock(type, new Location(null, x, y, z));
  }

  public void addBlock(Material type, @Nullable Integer blockSubCategory, int x, int y, int z) {
    addBlock(type, blockSubCategory, new Location(null, x, y, z));
  }

  public void addBlock(BlockData blockData, int x, int y, int z) {
    addBlock(blockData, new Location(null, x, y, z));
  }

  public void addBackwardProofBlock(Material type, @Nullable Integer blockSubCategory, @Nullable String blockData,
                                    Location location) {
    if (IS_ABOVE_1_13) {
      if (blockData != null) {
        addBlock(type.createBlockData(blockData), location);
      } else {
        addBlock(type, location);
      }
    } else {
      addBlock(type, blockSubCategory, location);
    }
  }

  public void addBackwardProofBlock(Material type, @Nullable Integer blockSubCategory, @Nullable String blockData, int x,
                                    int y, int z) {
    addBackwardProofBlock(type, blockSubCategory, blockData, new Location(null, x, y, z));
  }

  /**
   * Gets the relative position of a block
   * relative to the chunk, encoded inside a short.
   */
  private short getRelativePosition(int x, int y, int z) {
    //Convert to location within chunk.
    x = x & 0xF;
    y = y & 0xF;
    z = z & 0xF;

    //Creates position from location within chunk
    return (short) (x << 8 | z << 4 | y);
  }

  public void send(Player... players) throws InvocationTargetException {
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
      blocksLocationsArray[i] = getRelativePosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    packet.getSectionPositions().write(0, new BlockPosition(packetChunkX, packetChunkY, packetChunkZ));
    packet.getBooleans().writeSafely(0, suppressLightUpdates);

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
