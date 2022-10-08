package ml.empee.mysticalBarriers.services.components;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import ml.empee.mysticalBarriers.helpers.PlayerContext;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;

public class BarrierRefresher implements Listener {

  private static final PlayerContext<HashSet<String>> playerContext = PlayerContext.get("visibleBarriers");

  private final BarriersService barriersService;

  public BarrierRefresher(Plugin plugin, BarriersService barriersService) {
    this.barriersService = barriersService;

    Bukkit.getScheduler().runTaskTimer(plugin, () -> {

      for(Player player : Bukkit.getOnlinePlayers()) {
        for(Barrier barrier : barriersService.findAllBarriers()) {
          if(!player.getWorld().equals(barrier.getWorld())) {
            return;
          }

          refresh(player, barrier);
        }
      }

    }, 0, 20);
  }

  private void refresh(Player player, Barrier barrier) {
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
  }

}
