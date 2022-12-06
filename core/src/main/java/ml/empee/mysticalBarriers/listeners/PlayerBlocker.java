package ml.empee.mysticalBarriers.listeners;

import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerBlocker extends AbstractListener {

  private final BarriersService barriersService;

  public PlayerBlocker(BarriersService barriersService) {
    this.barriersService = barriersService;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    Barrier barrier = barriersService.findBarrierAt(event.getTo());
    if (barrier != null && !barrier.isHiddenFor(player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    Barrier barrier = barriersService.findBarrierAt(event.getTo());
    if (barrier != null && !barrier.isHiddenFor(event.getPlayer())) {
      event.getPlayer().sendMessage("Teleport triggered by " + event.hashCode());
      event.setCancelled(true);
    }
  }

  //TODO Prevent endepearl from getting through barriers (Configurable)
  //TODO Prevent choruis fruit from being eaten when 8blocks from a barrier (configurable)

}
