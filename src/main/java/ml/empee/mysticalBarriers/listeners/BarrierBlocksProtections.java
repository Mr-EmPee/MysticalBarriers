package ml.empee.mysticalBarriers.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.utils.helpers.ProtocolLibHelper;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import ml.empee.mysticalBarriers.utils.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BarrierBlocksProtections extends AbstractListener {

  private final ProtocolLibHelper protocolLib = new ProtocolLibHelper();

  private final BarriersService barriersService;

  public BarrierBlocksProtections(BarriersService barriersService) {
    this.barriersService = barriersService;

    protocolLib.registerListener(PacketType.Play.Server.BLOCK_CHANGE, this::refreshBarrierBlocksOnServerChangePacket);
  }

  @Override
  protected void onUnregister() {
    protocolLib.unregisterAll();
  }

  @EventHandler
  public void cancelOnBarrierBlockPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      Logger.debug("Player %s tried to place a block on a barrier", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelOnBarrierBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      Logger.debug("Player %s tried to break a barrier block", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelPistonMoveEvent(BlockPistonExtendEvent event) {
    List<Block> affectedBlocks = event.getBlocks();
    Barrier barrier;
    if(affectedBlocks.isEmpty()) {
       barrier = barriersService.findBarrierAt(event.getBlock().getLocation().add(event.getDirection().getDirection()));
    } else {
      barrier = barriersService.findBarrierAt(
          affectedBlocks.get(affectedBlocks.size()-1).getLocation().add(event.getDirection().getDirection())
      );
    }

    if(barrier != null) {
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
    return LocationUtils.getGreatestAxisDistance(
        player.getLocation().getBlock().getLocation(),
        targetLoc
    ) <= barrier.getActivationRange();
  }

}
