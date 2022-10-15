package ml.empee.mysticalBarriers.services.components;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;

import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.helpers.EmpeePlugin;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import ml.empee.mysticalBarriers.utils.Logger;

public class BarrierGuard implements Listener {

  private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private final BarriersService barriersService;

  public BarrierGuard(EmpeePlugin plugin, BarriersService barriersService) {
    this.barriersService = barriersService;

    protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.BLOCK_DIG) {
      @Override
      public void onPacketReceiving(PacketEvent event) {
        onPlayerBreak(event);
      }
    });
    protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.BLOCK_CHANGE) {
      @Override
      public void onPacketSending(PacketEvent event) {
        onBlockChange(event);
      }
    });

  }

  public void onPlayerBreak(PacketEvent event) {
    PacketContainer packet = event.getPacket();
    Player player = event.getPlayer();
    if(
        player.getGameMode() != GameMode.CREATIVE
        &&
        packet.getPlayerDigTypes().read(0) != EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK
    ) {
      return;
    }

    Location blockLocation = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
    Barrier barrier = barriersService.findBarrierAt(blockLocation);
    if (barrier != null) {
      Logger.debug("Player %s tried to break a barrier block", player.getName());
      event.setCancelled(true);

      Material block = blockLocation.getBlock().getType();
      if (Material.AIR == block) {
        block = barrier.getMaterial();
      }

      MultiBlockPacket blockPacket = new MultiBlockPacket(blockLocation, true);
      blockPacket.addBlock(block, blockLocation);
      try {
        blockPacket.send(player);
        Logger.debug("Refreshed barrier block for player %s", player.getName());
      } catch (InvocationTargetException e) {
        throw new MysticalBarrierException("Error while sending multi-block packet", e);
      }
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

  public void onBlockChange(PacketEvent event) {
    PacketContainer packet = event.getPacket();
    Player player = event.getPlayer();

    Location blockLocation = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
    if (blockLocation.getBlock().getType() != Material.AIR) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(blockLocation);
    if (barrier != null) {
      Logger.debug("Server is trying to change a barrier block");
      Material material = packet.getBlockData().read(0).getType();
      if (barrier.isHiddenFor(player)) {

        if (material == Material.AIR) {
          return;
        }

      } else {

        Logger.debug("Checking if the block is valid for the player %s", player.getName());
        if (material == barrier.getMaterial() || isJustifiedAir(material, player, blockLocation, barrier)) {
          Logger.debug("Refreshed barrier block for player %s", player.getName());
          return;
        }

      }

      event.setCancelled(true);
    }

  }

  /**
   * This method is used to check if the server is trying to edit a barrier block
   * using the air material and the block that is being edited isn't inside the
   * barrier activation radius
   */
  private static boolean isJustifiedAir(
      Material blockMaterial, Player player,
      Location blockLocation, Barrier barrier
  ) {
    return blockMaterial == Material.AIR
           &&
           LocationUtils.fastBlockDistance(
               player.getLocation().getBlock().getLocation(),
               blockLocation
           ) > barrier.getActivationRange();
  }

}
