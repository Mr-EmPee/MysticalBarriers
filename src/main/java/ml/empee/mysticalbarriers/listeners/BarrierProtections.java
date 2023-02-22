package ml.empee.mysticalbarriers.listeners;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalbarriers.model.Barrier;
import ml.empee.mysticalbarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalbarriers.services.BarriersService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import ml.empee.mysticalbarriers.utils.helpers.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BarrierProtections implements RegisteredListener, Bean {

  private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private final PacketBlockProtection packetBlockProtection;
  private final BarriersService barriersService;
  private final Logger logger;

  public BarrierProtections(
    JavaPlugin plugin, BarriersService barriersService, Logger logger
  ) {
    this.packetBlockProtection = new PacketBlockProtection(plugin);
    this.barriersService = barriersService;
    this.logger = logger;
  }

  /**
   * This method is used to check if the server is trying to edit a barrier block using the air material and
   * the block that is being edited isn't inside the barrier activation radius
   */
  private static boolean isWithinBarrierRange(
    Player player, Location targetLoc, Barrier barrier
  ) {
    return LocationUtils.getGreatestAxisDistance(
      player.getLocation().getBlock().getLocation(),
      targetLoc
    ) <= barrier.getActivationRange();
  }

  public void onStart() {
    protocolManager.addPacketListener(packetBlockProtection);
  }

  public void onStop() {
    protocolManager.removePacketListener(packetBlockProtection);
  }

  @EventHandler
  public void cancelOnBarrierBlockPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      logger.debug("Player %s tried to place a block on a barrier", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelOnBarrierBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    Barrier barrier = barriersService.findBarrierAt(block.getLocation());
    if (barrier != null) {
      logger.debug("Player %s tried to break a barrier block", event.getPlayer().getName());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelPistonMoveEvent(BlockPistonExtendEvent event) {
    List<Block> affectedBlocks = event.getBlocks();
    Barrier barrier;
    if (affectedBlocks.isEmpty()) {
      barrier = barriersService.findBarrierAt(
        event.getBlock().getLocation().add(event.getDirection().getDirection())
      );
    } else {
      barrier = barriersService.findBarrierAt(
        affectedBlocks.get(affectedBlocks.size() - 1).getLocation().add(event.getDirection().getDirection())
      );
    }

    if (barrier != null) {
      event.setCancelled(true);
    }
  }

  /**
   * Necessary packet block manipulation protection for when the server cancel an event
   **/
  private class PacketBlockProtection extends PacketAdapter {

    public PacketBlockProtection(JavaPlugin plugin) {
      super(plugin, Play.Server.BLOCK_CHANGE);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
      PacketContainer packet = event.getPacket();
      Player player = event.getPlayer();

      Location blockLocation = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
      Block block = blockLocation.getBlock();
      if (block.getType() != Material.AIR) {
        return;
      }

      Barrier barrier = barriersService.findBarrierAt(blockLocation);
      if (barrier != null) {
        logger.debug("Server is trying to change a barrier block");

        if (barrier.isHiddenFor(player) || !isWithinBarrierRange(player, blockLocation, barrier)) {
          logger.debug("The block was hidden for the player or was out of range");
          return;
        }

        event.setCancelled(true);
        MultiBlockPacket multiBlockPacket = new MultiBlockPacket(blockLocation, true);
        multiBlockPacket.addBackwardProofBlock(
          barrier.getMaterial(), null, barrier.getBlockData(), blockLocation
        );

        try {
          multiBlockPacket.send(player);
          logger.debug("The barrier block has been re-sent to the player");
        } catch (InvocationTargetException e) {
          throw new MysticalBarrierException("Error while sending some barrier blocks", e);
        }
      }
    }
  }

}
