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
import ml.empee.mysticalBarriers.utils.helpers.DynamicListener;
import ml.empee.mysticalBarriers.utils.reflection.ReflectionUtils;
import ml.empee.mysticalBarriers.utils.reflection.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
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

    if (ServerVersion.isGreaterThan(1, 9)) {
      cancelOnLingeringSplashInsideBarrier()
          .eventClass(LingeringPotionSplashEvent.class)
          .ignoreCancelled(true)
          .listener(this)
          .register();
    }

    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      followedEntities.entrySet().removeIf(entry -> reflectProjectileOnBarrierTouch(entry.getKey(), entry.getValue()));
    }, 0, 1);
  }

  private boolean reflectProjectileOnBarrierTouch(Entity entity, Player shooter) {
    if (!entity.isValid()) {
      return true;
    } else if (!entity.isOnGround()) {
      List<Location> traveledBlocks = LocationUtils.getBlocksBetween(entity.getLocation(), entity.getVelocity());

      Barrier barrier = null;
      for (Location location : traveledBlocks) {
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
            if (ServerVersion.isGreaterThan(1, 9)) {
              shooter.playSound(entity.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
            } else {
              shooter.playSound(entity.getLocation(), Sound.valueOf("ITEM_BREAK"), 1, 1);
            }
          }

          entity.setVelocity(entity.getVelocity().multiply(-0.25));
        } else {
          entity.setVelocity(new Vector(0, -0.1, 0));
        }

        return true;
      }
    }

    return false;
  }

  @EventHandler(ignoreCancelled = true)
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
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

  @EventHandler(ignoreCancelled = true)
  public void onDropNearBarrier(PlayerDropItemEvent event) {
    List<Barrier> barriers = barriersService.findBarriersWithinRangeAt(event.getItemDrop().getLocation(), 8);
    if (!barriers.isEmpty() && barriers.stream().anyMatch(b -> !b.isHiddenFor(event.getPlayer()))) {
      followedEntities.put(event.getItemDrop(), event.getPlayer());
    }
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
  public void cancelOnChorusTeleportationNearBarrier(PlayerTeleportEvent event) {
    if (!config.getBlockChorusFruitTeleportation() ||
        !event.getCause().name().equals("CHORUS_FRUIT")) {
      return;
    }

    List<Barrier> barriers = barriersService.findBarriersWithinRangeAt(event.getFrom(), 8);
    if (!barriers.isEmpty() && barriers.stream().anyMatch(b -> !b.isHiddenFor(event.getPlayer()))) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void cancelOnEntityDamageInsideBarrier(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player) {
      Barrier barrier = barriersService.findBarrierAt(event.getEntity().getLocation());
      if (barrier != null && !barrier.isHiddenFor((Player) event.getDamager())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void cancelOnProjectileDamageInsideBarrier(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Projectile)) {
      return;
    }

    Projectile projectile = (Projectile) event.getDamager();
    if (!(projectile.getShooter() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(event.getEntity().getLocation());
    if (barrier == null || barrier.isHiddenFor((Player) projectile.getShooter())) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
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

  @EventHandler(ignoreCancelled = true)
  public void cancelOnEntityMountInsideBarrier(EntityMountEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(event.getMount().getLocation());
    if (barrier != null && !barrier.isHiddenFor((Player) event.getEntity())) {
      event.setCancelled(true);
    }
  }

  public DynamicListener<?> cancelOnLingeringSplashInsideBarrier() {
    return new DynamicListener<LingeringPotionSplashEvent>() {
      @Override
      public void onEvent(LingeringPotionSplashEvent event) {
        ThrownPotion potion = ReflectionUtils.getThrownPotion(event);
        if (!(potion.getShooter() instanceof Player)) {
          return;
        }

        Barrier barrier = barriersService.findBarrierAt(potion.getLocation());
        if (barrier != null && !barrier.isHiddenFor((Player) potion.getShooter())) {
          event.getAreaEffectCloud().remove();
        }
      }
    };
  }
}
