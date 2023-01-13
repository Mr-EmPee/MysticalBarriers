package ml.empee.mysticalBarriers.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import ml.empee.ioc.Stoppable;
import ml.empee.ioc.annotations.Bean;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

@Bean
public class BarrierSpawner implements Listener, Stoppable {

  private final HashMap<Player, HashSet<String>> visibleBarriers = new HashMap<>();
  private final BukkitTask bukkitTask;
  private final BarriersService barriersService;

  public BarrierSpawner(MysticalBarriersPlugin plugin, BarriersService barriersService) {
    this.barriersService = barriersService;

    bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, refreshBarriers(), 0, 20);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void sendBarriersBlocksOnPlayerMove(PlayerMoveEvent event) {
    if (!LocationUtils.hasChangedBlock(event.getFrom(), event.getTo())) {
      return;
    }

    sendBarriersBlocks(event.getTo(), event.getFrom(), event.getPlayer());
  }

  public void sendBarriersBlocks(Location toLoc, Location fromLoc, Player player) {
    for (Barrier barrier : barriersService.findAllBarriers()) {
      if (barrier.isHiddenFor(player) || (!barrier.isWithinRange(toLoc, null) && !barrier.isWithinRange(fromLoc, null))) {
        continue;
      }

      sendBarrierBlocks(player, barrier, fromLoc, toLoc);
    }
  }

  public void sendBarrierBlocks(Player player, Barrier barrier, Location fromLoc, Location toLoc) {
    MultiBlockPacket packet = new MultiBlockPacket(toLoc, false);

    List<Location> visibleBarrierBlocks = new ArrayList<>();
    barrier.forEachVisibleBarrierBlock(toLoc, visibleBarrierBlocks::add);

    barrier.forEachVisibleBarrierBlock(fromLoc, (loc) -> {
      if (!visibleBarrierBlocks.remove(loc)) {
        packet.addBlock(Material.AIR, loc);
      }
    });

    for (Location loc : visibleBarrierBlocks) {
      packet.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), loc);
    }

    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException("Error while sending the barriers packet", e);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void refreshOnTeleport(PlayerTeleportEvent event) {
    Location fromLoc = event.getFrom();
    Location toLoc = event.getTo();
    if (toLoc == null) {
      return;
    }

    for (Barrier barrier : barriersService.findBarriersWithinRangeAt(fromLoc, null)) {
      if (barrier.isHiddenFor(event.getPlayer())) {
        continue;
      }

      barriersService.despawnBarrierAt(fromLoc, barrier, event.getPlayer());
    }

    for (Barrier barrier : barriersService.findBarriersWithinRangeAt(toLoc, null)) {
      if (barrier.isHiddenFor(event.getPlayer())) {
        continue;
      }

      barriersService.spawnBarrierAt(toLoc, barrier, event.getPlayer());
    }
  }

  private Runnable refreshBarriers() {
    return () -> Bukkit.getOnlinePlayers().forEach(player -> barriersService.findAllBarriers().forEach(barrier -> {
      if (!player.getWorld().equals(barrier.getWorld())) {
        return;
      }

      HashSet<String> barriers = visibleBarriers.computeIfAbsent(player, p -> new HashSet<>());
      if (barrier.isHiddenFor(player)) {
        if (barriers.remove(barrier.getId())) {
          barriersService.refreshBarrierFor(player, barrier);
        }
      } else {
        if (barriers.add(barrier.getId())) {
          barriersService.refreshBarrierFor(player, barrier);
        }
      }
    }));
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    visibleBarriers.remove(event.getPlayer());
  }

  @Override
  public void stop() {
    bukkitTask.cancel();
  }

}
