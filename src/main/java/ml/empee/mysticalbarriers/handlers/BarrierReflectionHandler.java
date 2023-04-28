package ml.empee.mysticalbarriers.handlers;

import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.config.BarriersConfig;
import ml.empee.mysticalbarriers.model.content.EntityTracker;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Optional;

/**
 * Handle projectile movement
 */

public class BarrierReflectionHandler extends EntityTracker<Entity> implements Bean, RegisteredListener {

  private final BarrierService barrierService;
  private final BarriersConfig config;

  public BarrierReflectionHandler(BarriersConfig config, BarrierService barrierService) {
    super();

    this.barrierService = barrierService;
    this.config = config;
  }

  @Override
  public boolean isEnabled() {
    return config.shouldRepelProjectiles();
  }


  /**
   * Repel any tracked projectile that will hit a barrier block
   */
  protected boolean run(Entity entity) {
    if (entity.isOnGround()) {
      return false;
    }

    ImpactPoint impact = findImpactLocation(entity);
    if (impact == null) {
      return false;
    }

    Player source = getSource(entity);
    if (source != null && !impact.barrier().isVisibleFor(source)) {
      return false;
    }

    //TODO: Make ceiling exception
    entity.getWorld().playSound(impact.location(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
    entity.setVelocity(entity.getVelocity().multiply(-1));
    return true;
  }

  private static Player getSource(Entity entity) {
    Player owner = null;
    if (entity instanceof Projectile projectile) {
      if (projectile.getShooter() instanceof Player) {
        owner = (Player) projectile.getShooter();
      }
    } else if (entity instanceof Item item) {
      if (item.getThrower() != null) {
        owner = Bukkit.getPlayer(item.getThrower());
      }
    }

    return owner;
  }

  /**
   * An entity can travel several blocks per ticks, this
   * method will check every block that otherwise would have been skipped
   */
  private ImpactPoint findImpactLocation(Entity entity) {
    Location end = entity.getLocation().add(entity.getVelocity());
    for (Location impact : LocationUtils.getBlocksArea(entity.getLocation(), end)) {
      Optional<Barrier> barrier = barrierService.findBarrierByBlock(impact.getBlock());
      if (barrier.isPresent()) {
        return new ImpactPoint(barrier.get(), impact);
      }
    }

    return null;
  }

  private record ImpactPoint(Barrier barrier, Location location) {}

  /**
   * Forget projectiles if it hits an entity
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onProjectileHitEntity(ProjectileHitEvent event) {
    if (event.getHitEntity() == null) {
      return;
    }

    forgetEntity(event.getEntity());
  }

  /**
   * Track launched projectiles
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onProjectileShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player) && config.shouldRepelOnlyPlayerProjectiles()) {
      return;
    }

    trackEntity(event.getEntity());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPlayerDrop(PlayerDropItemEvent event) {
    trackEntity(event.getItemDrop());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onItemPickup(PlayerAttemptPickupItemEvent event) {
    forgetEntity(event.getItem());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onItemPickup(EntityPickupItemEvent event) {
    forgetEntity(event.getItem());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onItemPickup(InventoryPickupItemEvent event) {
    forgetEntity(event.getItem());
  }

}
