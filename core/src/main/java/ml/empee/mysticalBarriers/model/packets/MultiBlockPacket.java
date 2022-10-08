package ml.empee.mysticalBarriers.model.packets;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import ml.empee.mysticalBarriers.utils.ArrayUtils;

public final class MultiBlockPacket {
    private static final PacketType type = PacketType.Play.Server.MULTI_BLOCK_CHANGE;

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final PacketContainer packet = new PacketContainer(type);

    private final boolean suppressLightUpdates;
    private final int packetChunkX;
    private final int packetChunkY;
    private final int packetChunkZ;

    private final ArrayList<Short> blocksLocations = new ArrayList<>();
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

    public void addBlock(Material type, int x, int y, int z) {

        int blockChunkX = x >> 4;
        int blockChunkY = y >> 4;
        int blockChunkZ = z >> 4;

        if(this.packetChunkX != blockChunkX || this.packetChunkY != blockChunkY || this.packetChunkZ != blockChunkZ) {
            if(subPacket == null) {
                subPacket = new MultiBlockPacket(blockChunkX, blockChunkY, blockChunkZ, suppressLightUpdates);
            }

            subPacket.addBlock(type, x, y, z);
        } else {
            blocksData.add(WrappedBlockData.createData(type));
            blocksLocations.add( getRelativePosition(x, y, z) );
        }

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
        if(blocksData.isEmpty() && subPacket == null) {
            return;
        }

        WrappedBlockData[] blocksDataArray = blocksData.toArray(new WrappedBlockData[0]);
        short[] blocksLocationsArray = ArrayUtils.toPrimitive(this.blocksLocations);

        packet.getSectionPositions().write(0, new BlockPosition(packetChunkX, packetChunkY, packetChunkZ) );
        packet.getBooleans().writeSafely(0, suppressLightUpdates);

        packet.getBlockDataArrays().write(0, blocksDataArray);
        packet.getShortArrays().write(0, blocksLocationsArray);

        for(Player player : players) {
            protocolManager.sendServerPacket(player, packet);
        }

        if(subPacket != null) {
            subPacket.send(players);
        }
    }

}
