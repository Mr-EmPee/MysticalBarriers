package core.handlers;

import core.model.Barrier;
import core.services.BarriersService;
import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import protocolib.ProtocolLibBarrierProtectionHandler;
import utils.PluginUtils;
import utils.ServerVersion;

@LightWired
public class BarrierProtectionsHandler implements Listener {

  private final BarriersService barriersService;
  private final JavaPlugin plugin;
  private final boolean hasProtocolLib;

  public BarrierProtectionsHandler(BarriersService barriersService, JavaPlugin plugin) {
    this.barriersService = barriersService;
    this.plugin = plugin;

    //Why 1.20.2 exception? Simple some cancelled events also sent adjacent blocks....
    hasProtocolLib = ServerVersion.isLowerThan(1, 20, 2) || PluginUtils.hasProtocolLib();
    if (hasProtocolLib) {
      registerProtocolBarrierProtectionHandler(barriersService, plugin);
    }
  }

  private static void registerProtocolBarrierProtectionHandler(BarriersService barriersService, JavaPlugin plugin) {
    var handler = new ProtocolLibBarrierProtectionHandler() {

      @Nullable
      protected BlockData getBlockDataAt(Player player, Location location) {
        Barrier barrier = barriersService.findBarrierAt(location).orElse(null);
        if (barrier == null) {
          return null;
        }

        if (barriersService.isHidden(barrier, player)) {
          return null;
        }

        return barrier.getBlockDataAt(location);
      }

    };

    handler.register(plugin);
  }

  @EventHandler
  public void onBarrierBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    if (player.isOp()) {
      return;
    }

    var block = event.getBlock();
    var barrier = barriersService.findBarrierAt(block.getLocation()).orElse(null);
    if (barrier == null) {
      return;
    }

    BlockData barrierBlock = barrier.getBlockDataAt(block.getLocation());
    if (barrierBlock == null) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onBarrierBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    if (player.isOp()) {
      return;
    }

    var block = event.getBlock();
    var barrier = barriersService.findBarrierAt(block.getLocation()).orElse(null);
    if (barrier == null) {
      return;
    }

    BlockData barrierBlock = barrier.getBlockDataAt(block.getLocation());
    if (barrierBlock == null) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onBarrierInteract(PlayerInteractEvent event) {
    if (hasProtocolLib) {
      return;
    }

    var block = event.getClickedBlock();
    if (block == null) {
      return;
    }

    var barrier = barriersService.findBarrierAt(block.getLocation()).orElse(null);
    if (barrier == null) {
      return;
    }

    Player player = event.getPlayer();
    if (barriersService.isHidden(barrier, player)) {
      return;
    }

    BlockData barrierBlock = barrier.getBlockDataAt(block.getLocation());
    if (barrierBlock == null) {
      return;
    }

    event.setCancelled(true);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      player.sendBlockChange(block.getLocation(), barrierBlock);
    }, 1);
  }

}
