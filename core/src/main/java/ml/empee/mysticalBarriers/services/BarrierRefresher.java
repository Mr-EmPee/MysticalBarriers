package ml.empee.mysticalBarriers.services;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import ml.empee.mysticalBarriers.helpers.PlayerContext;
import ml.empee.mysticalBarriers.model.Barrier;

public class BarrierRefresher extends AbstractService {

  private static final PlayerContext<HashSet<String>> playerContext = PlayerContext.get("visibleBarriers");

  private final BarriersService barriersService;
  private final BukkitTask bukkitTask;

  public BarrierRefresher(Plugin plugin, BarriersService barriersService) {
    this.barriersService = barriersService;

    bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

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

  @Override
  protected void onDisable() {
    bukkitTask.cancel();
  }
}
