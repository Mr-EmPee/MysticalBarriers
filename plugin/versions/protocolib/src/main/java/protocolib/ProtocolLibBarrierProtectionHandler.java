package protocolib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public abstract class ProtocolLibBarrierProtectionHandler {


  public void register(JavaPlugin plugin) {
    ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.BLOCK_CHANGE) {
      public void onPacketSending(PacketEvent event) {
        ProtocolLibBarrierProtectionHandler.this.onPacketSending(event);
      }
    });
  }

  @Nullable
  protected abstract BlockData getBlockDataAt(Player player, Location location);

  public void onPacketSending(PacketEvent event) {
    var packet = event.getPacket();
    var player = event.getPlayer();

    Location location = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
    Block serverBlock = location.getBlock();
    if (serverBlock.getType() != Material.AIR) {
      return;
    }

    BlockData barrierBlock = getBlockDataAt(player, location);
    if (barrierBlock == null) {
      return;
    }

    packet.getBlockData().write(0, WrappedBlockData.createData(barrierBlock));
  }

}

