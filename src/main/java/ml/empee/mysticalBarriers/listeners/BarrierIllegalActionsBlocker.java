package ml.empee.mysticalBarriers.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import ml.empee.ioc.annotations.Bean;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.config.Config;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import ml.empee.mysticalBarriers.utils.MCLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityMountEvent;

@Bean
public class BarrierIllegalActionsBlocker implements Listener {

  private final HashMap<Entity, Player> followedEntities = new HashMap<>();
  private final BarriersService barriersService;
  private final Config config;

  public BarrierIllegalActionsBlocker(MysticalBarriersPlugin plugin, BarriersService barriersService, Config config) {
    this.barriersService = barriersService;
    this.config = config;

    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      followedEntities.entrySet().removeIf(entry -> checkFollowedEntity(entry.getKey(), entry.getValue()));
    }, 0, 1);
  }

  private boolean checkFollowedEntity(Entity entity, Player shooter) {
    if (!entity.isValid()) {
      return true;
    } else if (!entity.isOnGround()) {
      List<Location> traveledBlocks = LocationUtils.getBlocksBetween(entity.getLocation(), entity.getVelocity());

      Barrier barrier = null;
      for(Location location : traveledBlocks) {
        barrier = barriersService.findBarrierAt(location);
        if (barrier != null) {
          break;
        }
      }

      if (barrier != null) {
        if (shooter != null && barrier.isHiddenFor(shooter)) {
          return false;
        }

        if (entity instanceof Projectile) {
          if (shooter != null) {
            shooter.playSound(entity.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
          }

          entity.remove();
        } else {
          entity.setVelocity(new Vector(0, -0.1, 0));
        }

        return true;
      }
    }

    return false;
  }

  @EventHandler
  public void removeProjectileOnMoveInsideBarrier(ProjectileLaunchEvent event) {
    if (!config.isProjectileMovementBlocked()) {
      return;
    }

    ProjectileSource shooter = event.getEntity().getShooter();
    if (!(shooter instanceof Player)) {
      if (config.shouldBlockOnlyPlayerProjectiles()) {
        return;
      }

      shooter = null;
    }

    followedEntities.put(event.getEntity(), (Player) shooter);
  }

  @EventHandler
  public void blockOnDropInsideBarrier(PlayerDropItemEvent event) {
    followedEntities.put(event.getItemDrop(), event.getPlayer());
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelOnPlayerMoveInsideBarrier(PlayerMoveEvent event) {
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
  public void cancelOnTeleportingInsideBarrier(PlayerTeleportEvent event) {
    Barrier barrier = barriersService.findBarrierAt(event.getTo());
    if (barrier != null && !barrier.isHiddenFor(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelOnChorusTeleportation(PlayerTeleportEvent event) {
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
  public void cancelOnEntityDamageInsideBarrier(EntityDamageByEntityEvent event) {
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
  public void cancelOnPotionSplashInsideBarrier(PotionSplashEvent potionSplashEvent) {
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
  public void cancelOnLingeringSplashInsideBarrier(
      LingeringPotionSplashEvent lingeringPotionSplashEvent
  ) {
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
}
