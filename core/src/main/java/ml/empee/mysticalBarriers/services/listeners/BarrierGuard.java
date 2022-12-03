package ml.empee.mysticalBarriers.services.listeners;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.helpers.EmpeePlugin;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import ml.empee.mysticalBarriers.utils.Logger;

public class BarrierGuard extends AbstractListener {

  private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private final BarriersService barriersService;

  private final PacketListener[] packetListeners;

  public BarrierGuard(EmpeePlugin plugin, BarriersService barriersService) {
    this.barriersService = barriersService;

    packetListeners = new PacketListener[] {
        new PacketAdapter(plugin, PacketType.Play.Server.BLOCK_CHANGE) {
          @Override
          public void onPacketSending(PacketEvent event) {
            onBlockChange(event);
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
  public void onBlockPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      Logger.debug("Player %s tried to place a block on a barrier", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      Logger.debug("Player %s tried to break a barrier block", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  public void onBlockChange(PacketEvent event) {
    PacketContainer packet = event.getPacket();
    Player player = event.getPlayer();

    Location blockLocation = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
    Block block = blockLocation.getBlock();
    if (block.getType() != Material.AIR) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(blockLocation);
    if (barrier != null) {
      Logger.debug("Server is trying to change a barrier block");

      if (barrier.isHiddenFor(player) || !isWithinBarrierRange(player, blockLocation, barrier)) {
        Logger.debug("The block was hidden for the player or was out of range");
        return;
      }

      event.setCancelled(true);
      MultiBlockPacket multiBlockPacket = new MultiBlockPacket(blockLocation, true);
      multiBlockPacket.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), blockLocation);

      try {
        multiBlockPacket.send(player);
        Logger.debug("The barrier block has been re-sent to the player");
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
    return LocationUtils.fastBlockDistance(
        player.getLocation().getBlock().getLocation(),
        targetLoc
    ) <= barrier.getActivationRange();
  }

}
