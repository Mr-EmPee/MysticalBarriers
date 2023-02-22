package ml.empee.mysticalbarriers.listeners;

import java.util.HashMap;
import java.util.HashSet;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.ioc.ScheduledTask;
import ml.empee.mysticalbarriers.model.Barrier;
import ml.empee.mysticalbarriers.services.BarriersService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BarrierRefresher extends ScheduledTask implements RegisteredListener, Bean {

  private final HashMap<Player, HashSet<String>> visibleBarriers = new HashMap<>();
  private final BarriersService barriersService;

  public BarrierRefresher(BarriersService barriersService) {
    super(0, 20, false);

    this.barriersService = barriersService;
  }

  public void run() {
    Bukkit.getOnlinePlayers().forEach(player -> barriersService.findAllBarriers().forEach(barrier -> {
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

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void refreshOnTeleport(PlayerTeleportEvent event) {
    Location fromLoc = event.getFrom();
    Location toLoc = event.getTo();
    if (toLoc == null) { //Legacy versions
      return;
    }

    for (Barrier barrier : barriersService.findBarriersWithinRangeAt(fromLoc, null)) {
      if (barrier.isHiddenFor(event.getPlayer())) {
        continue;
      }

      barriersService.hideBarrierAt(fromLoc, barrier, event.getPlayer());
    }

    for (Barrier barrier : barriersService.findBarriersWithinRangeAt(toLoc, null)) {
      if (barrier.isHiddenFor(event.getPlayer())) {
        continue;
      }

      barriersService.showBarrierAt(toLoc, barrier, event.getPlayer());
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    visibleBarriers.remove(event.getPlayer());
  }

}
