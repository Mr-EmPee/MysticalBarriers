package ml.empee.mysticalbarriers.handlers;

import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.config.BarriersConfig;
import ml.empee.mysticalbarriers.model.content.EntityTracker;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

/**
 * Handle projectile movement
 */

public class ProjectileReflectionHandler extends EntityTracker<Projectile> implements Bean, RegisteredListener {

  private final BarrierService barrierService;
  private final BarriersConfig config;

  public ProjectileReflectionHandler(BarriersConfig config, BarrierService barrierService) {
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
  protected boolean run(Projectile entity) {
    if (entity.isOnGround()) {
      return false;
    }

    Location impact = findImpactLocation(entity);
    if (impact == null) {
      return false;
    }

    Barrier barrier = barrierService.findBarrierByBlock(impact.getBlock()).get();

    Location min = barrier.getLowestCorner();
    Location max = barrier.getGreatestCorner();

    //If it impacts with the ceiling
    if (impact.getY() == max.getY()) {
      Vector distanceFromMax = LocationUtils.getDistance(impact, max);
      Vector distanceFromMin = LocationUtils.getDistance(impact, min);
      Vector velocity = new Vector();

      if (distanceFromMin.getX() < distanceFromMax.getX()) {
        velocity.setX(impact.getX() - min.getX());
      } else {
        velocity.setX(impact.getX() - max.getX());
      }

      if (distanceFromMin.getZ() < distanceFromMax.getZ()) {
        velocity.setZ(impact.getZ() - min.getZ());
      } else {
        velocity.setZ(impact.getZ() - max.getZ());
      }

      if (Math.abs(velocity.getX()) > Math.abs(velocity.getZ())) {
        velocity.setX(0);
      } else {
        velocity.setZ(0);
      }

      entity.setVelocity(velocity.normalize().multiply(-1));
    } else {
      entity.setVelocity(new Vector(0, 0, 0));
    }

    return true;
  }

  /**
   * An entity can travel several blocks per ticks, this
   * method will check every block that otherwise would have been skipped
   */
  private Location findImpactLocation(Entity entity) {
    Location start = entity.getLocation();
    if (barrierService.findBarrierByBlock(start.getBlock()).isPresent()) {
      return entity.getLocation();
    }

    Location end = start.clone().add(entity.getVelocity());
    for (Location impact : LocationUtils.getBlocksArea(start, end)) {
      if (barrierService.findBarrierByBlock(impact.getBlock()).isPresent()) {
        return impact;
      }
    }

    return null;
  }

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
}
