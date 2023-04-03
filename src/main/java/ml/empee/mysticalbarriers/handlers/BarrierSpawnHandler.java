package ml.empee.mysticalbarriers.handlers;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import ml.empee.mysticalbarriers.utils.PaperUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handler used to spawn the barrier blocks near a player
 */

@RequiredArgsConstructor
public class BarrierSpawnHandler implements Bean, RegisteredListener {

  private final BarrierService barrierService;

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (LocationUtils.isSameBlock(event.getFrom(), event.getTo())) {
      return;
    }

    Player player = event.getPlayer();

    //TODO Improve performance
    for (Barrier barrier : barrierService.findAll()) {
      if (barrier.isNear(event.getFrom())) {
        barrier.hideBarrier(player, event.getFrom());
      }

      if (barrier.isNear(event.getTo()) && barrier.isVisibleFor(player)) {
        barrier.showBarrier(player, event.getTo());
      }
    }

  }

  /**
   * Listeners registered only if the server is running paper
   */
  @RequiredArgsConstructor
  private static class PaperListeners implements Bean, RegisteredListener {
    private final BarrierService barrierService;

    @Override
    public boolean isEnabled() {
      return PaperUtils.IS_RUNNING_PAPER;
    }

    @EventHandler
    public void onPlayerLoadChunk(PlayerChunkLoadEvent event) {
      Player player = event.getPlayer();
      for (Barrier barrier : barrierService.findBarrierNear(player.getLocation())) {
        if (barrier.isVisibleFor(player)) {
          barrier.showBarrier(player, player.getLocation());
        }
      }
    }
  }

}
