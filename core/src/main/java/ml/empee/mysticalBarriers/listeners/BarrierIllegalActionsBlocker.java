package ml.empee.mysticalBarriers.listeners;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ml.empee.mysticalBarriers.config.Config;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
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
      if (player.getVehicle() != null) {
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
    if (!config.getBlockChorusFruitTeleportation() ||
        !event.getCause().name().equals("CHORUS_FRUIT")) {
      return;
    }

    List<Barrier> barriers = barriersService.findBarriersWithinRangeAt(event.getFrom(), 8);
    if (!barriers.isEmpty() && barriers.stream().anyMatch(b -> !b.isHiddenFor(event.getPlayer()))) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelEntityDamageInsideBarrier(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player) {
      Barrier barrier = barriersService.findBarrierAt(event.getEntity().getLocation());
      if (barrier != null && !barrier.isHiddenFor((Player) event.getDamager())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void cancelOnProjectileHit(ProjectileHitEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player)) {
      return;
    }

    Barrier barrier;
    if (event.getHitEntity() != null) {
      barrier = barriersService.findBarrierAt(
          event.getHitEntity().getLocation().getBlock().getLocation()
      );
    } else if (event.getHitBlock() != null) {
      barrier = LocationUtils.forEachAdjacentBlock(event.getHitBlock().getLocation())
          .map(barriersService::findBarrierAt)
          .filter(Objects::nonNull).findFirst().orElse(null);
    } else {
      barrier = barriersService.findBarrierAt(event.getEntity().getLocation());
    }

    if (barrier != null && !barrier.isHiddenFor((Player) event.getEntity().getShooter())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void cancelPotionSplashEvent(PotionSplashEvent potionSplashEvent) {
    if (!(potionSplashEvent.getEntity().getShooter() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(potionSplashEvent.getEntity().getLocation());
    if (barrier != null &&
        !barrier.isHiddenFor((Player) potionSplashEvent.getEntity().getShooter())) {
      potionSplashEvent.getAffectedEntities().forEach(entity -> {
        potionSplashEvent.setIntensity(entity, 0);
      });
    }
  }

  @EventHandler
  public void cancelLingeringPotionSplashEvent(
      LingeringPotionSplashEvent lingeringPotionSplashEvent) {
    if (!(lingeringPotionSplashEvent.getEntity().getShooter() instanceof Player)) {
      return;
    }

    Barrier barrier =
        barriersService.findBarrierAt(lingeringPotionSplashEvent.getEntity().getLocation());
    if (barrier != null &&
        !barrier.isHiddenFor((Player) lingeringPotionSplashEvent.getEntity().getShooter())) {
      lingeringPotionSplashEvent.getAreaEffectCloud().remove();
    }
  }

  @EventHandler
  public void cancelOnEntityMountInsideBarrier(EntityMountEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(event.getMount().getLocation());
    if (barrier != null && !barrier.isHiddenFor((Player) event.getEntity())) {
      event.setCancelled(true);
    }
  }

  //TODO Prevent projectiles from getting through barriers (Configurable)

}
