package ml.empee.mysticalBarriers.listeners;

import java.util.HashSet;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerContext;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

public class BarrierBlocksUpdater extends AbstractListener {

  private final PlayerContext<HashSet<String>> playerVisibleBarriers = PlayerContext.get("playerVisibleBarriers");

  private final BarriersService barriersService;
  private final BukkitTask bukkitTask;

  public BarrierBlocksUpdater(BarriersService barriersService) {
    this.barriersService = barriersService;

    bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, refreshOnPermissionChange(), 0, 20);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void refreshOnTeleport(PlayerTeleportEvent event) {
    Location fromLoc = event.getFrom();
    Location toLoc = event.getTo();
    if(toLoc == null) {
      return;
    }

    for(Barrier barrier : barriersService.findBarriersWithinRangeAt(fromLoc, null)) {
      barriersService.despawnBarrierAt(fromLoc, barrier, event.getPlayer());
    }

    for(Barrier barrier : barriersService.findBarriersWithinRangeAt(toLoc, null)) {
      barriersService.spawnBarrierAt(toLoc, barrier, event.getPlayer());
    }
  }

  public Runnable refreshOnPermissionChange() {
    return () -> Bukkit.getOnlinePlayers().forEach(player -> barriersService.findAllBarriers().forEach(barrier -> {
      if(!player.getWorld().equals(barrier.getWorld())) {
        return;
      }

      HashSet<String> visibleBarriers = playerVisibleBarriers.getOrPut(PlayerData.of(player, new HashSet<>())).get();
      if(barrier.isHiddenFor(player)) {
        if(visibleBarriers.remove(barrier.getId())) {
          barriersService.refreshBarrierFor(player, barrier);
        }
      } else {
        if(visibleBarriers.add(barrier.getId())) {
          barriersService.refreshBarrierFor(player, barrier);
        }
      }
    }));
  }

  @Override
  protected void onUnregister() {
    bukkitTask.cancel();
  }
}
