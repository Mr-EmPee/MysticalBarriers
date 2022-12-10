package ml.empee.mysticalBarriers.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import java.lang.reflect.InvocationTargetException;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import ml.empee.mysticalBarriers.utils.MCLogger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BarrierBlocksProtections extends AbstractListener {

  private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private final PacketListener[] packetListeners;

  private final BarriersService barriersService;

  public BarrierBlocksProtections(BarriersService barriersService) {
    this.barriersService = barriersService;

    packetListeners = new PacketListener[] {
        new PacketAdapter(plugin, PacketType.Play.Server.BLOCK_CHANGE) {
          @Override
          public void onPacketSending(PacketEvent event) {
            refreshBarrierBlocksOnServerChangePacket(event);
          }
        }
    };

    for (PacketListener packetListener : packetListeners) {
      protocolManager.addPacketListener(packetListener);
    }
  }

  @Override
  protected void onUnregister() {
    for (PacketListener packetListener : packetListeners) {
      protocolManager.removePacketListener(packetListener);
    }
  }

  @EventHandler
  public void cancelOnBarrierBlockPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      MCLogger.debug("Player %s tried to place a block on a barrier", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelOnBarrierBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      MCLogger.debug("Player %s tried to break a barrier block", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelPistonMoveEvent(BlockPistonExtendEvent event) {
    if(event.getBlocks().stream().anyMatch(block -> barriersService.findBarrierAt(block.getLocation()) != null)) {
      event.setCancelled(true);
    }
  }

  /**
   * Fired because when you cancel a block place/break event on spigot the server
   * will send the updated block and its adjacent blocks to the client.
   */
  public void refreshBarrierBlocksOnServerChangePacket(PacketEvent event) {
    PacketContainer packet = event.getPacket();
    Player player = event.getPlayer();

    Location blockLocation = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
    Block block = blockLocation.getBlock();
    if (block.getType() != Material.AIR) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(blockLocation);
    if (barrier != null) {
      MCLogger.debug("Server is trying to change a barrier block");

      if (barrier.isHiddenFor(player) || !isWithinBarrierRange(player, blockLocation, barrier)) {
        MCLogger.debug("The block was hidden for the player or was out of range");
        return;
      }

      event.setCancelled(true);
      MultiBlockPacket multiBlockPacket = new MultiBlockPacket(blockLocation, true);
      multiBlockPacket.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), blockLocation);

      try {
        multiBlockPacket.send(player);
        MCLogger.debug("The barrier block has been re-sent to the player");
      } catch (InvocationTargetException e) {
        throw new MysticalBarrierException("Error while sending some barrier blocks", e);
      }
    }

  }

  /**
   * This method is used to check if the server is trying to edit a barrier block
   * using the air material and the block that is being edited isn't inside the
   * barrier activation radius
   */
  private static boolean isWithinBarrierRange(
      Player player, Location targetLoc, Barrier barrier
  ) {
    return LocationUtils.getGreatestAxisDistance(
        player.getLocation().getBlock().getLocation(),
        targetLoc
    ) <= barrier.getActivationRange();
  }

}