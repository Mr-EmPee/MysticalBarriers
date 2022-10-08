package ml.empee.mysticalBarriers.services.components;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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

import ml.empee.mysticalBarriers.helpers.EmpeePlugin;
import ml.empee.mysticalBarriers.model.Barrier;
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

      BlockData block = blockLocation.getBlock().getBlockData();
      if (Material.AIR == block.getMaterial()) {
        block = barrier.getMaterial().createBlockData();
      }

      player.sendBlockChange(blockLocation, block);
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
      Material material = packet.getBlockData().read(0).getType();
      if (barrier.isHiddenFor(player)) {

        if (material == Material.AIR) {
          return;
        }

      } else {

        if (material == barrier.getMaterial() || isJustifiedAir(material, player, blockLocation, barrier)) {
          return;
        }

      }

      Logger.debug("The server has tried to edit a barrier block");
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
