package ml.empee.mysticalBarriers.listeners;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.empee.mysticalBarriers.config.Config;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.entity.EntityMountEvent;

@RequiredArgsConstructor
public class BarrierIllegalActionsBlocker extends AbstractListener {

  private final BarriersService barriersService;
  private final Config config;

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelMovementWhenInsideBarrier(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    Barrier barrier = barriersService.findBarrierAt(event.getTo());
    if (barrier != null && !barrier.isHiddenFor(player)) {
      event.setCancelled(true);
      if(player.getVehicle() != null) {
        player.getVehicle().eject();
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelTeleportingInsideBarrier(PlayerTeleportEvent event) {
    Barrier barrier = barriersService.findBarrierAt(event.getTo());
    if (barrier != null && !barrier.isHiddenFor(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelChorusTeleportation(PlayerTeleportEvent event) {
    if(!config.getBlockChorusFruitTeleportation() || !event.getCause().name().equals("CHORUS_FRUIT")) {
      return;
    }

    List<Barrier> barriers = barriersService.findBarriersWithinRangeAt(event.getFrom(), 8);
    if (!barriers.isEmpty() && barriers.stream().anyMatch(b -> !b.isHiddenFor(event.getPlayer()))) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelOnEntityMountInsideBarrier(EntityMountEvent event) {
    if(!(event.getEntity() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(event.getMount().getLocation());
    if(barrier != null && !barrier.isHiddenFor((Player) event.getEntity())) {
      event.setCancelled(true);
    }
  }

  //TODO Prevent projectiles from getting through barriers (Configurable)

}
