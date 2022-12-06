package ml.empee.mysticalBarriers.listeners;

import java.util.HashSet;
import ml.empee.mysticalBarriers.helpers.PlayerContext;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

public class BarrierRefresher extends AbstractListener {

  private static final PlayerContext<HashSet<String>> playerContext = PlayerContext.get("visibleBarriers");

  private final BarriersService barriersService;
  private final BukkitTask bukkitTask;

  public BarrierRefresher(BarriersService barriersService) {
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

    event.getPlayer().sendMessage("Refresh triggered by " + event.hashCode());

    for(Barrier barrier : barriersService.findBarriersWithinRangeAt(fromLoc)) {
      barriersService.despawnBarrierAt(fromLoc, barrier, event.getPlayer());
    }

    for(Barrier barrier : barriersService.findBarriersWithinRangeAt(toLoc)) {
      barriersService.spawnBarrierAt(toLoc, barrier, event.getPlayer());
    }
  }

  public Runnable refreshOnPermissionChange() {
    return () -> Bukkit.getOnlinePlayers().forEach(player -> barriersService.findAllBarriers().forEach(barrier -> {
      if(!player.getWorld().equals(barrier.getWorld())) {
        return;
      }

      HashSet<String> visibleBarriers = playerContext.getOrPut(player, new HashSet<>());
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
