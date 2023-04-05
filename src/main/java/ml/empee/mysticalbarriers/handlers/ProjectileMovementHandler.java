package ml.empee.mysticalbarriers.handlers;

import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.ioc.ScheduledTask;
import ml.empee.mysticalbarriers.config.BarriersConfig;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle projectile movement
 */

public class ProjectileMovementHandler extends ScheduledTask implements Bean, RegisteredListener {

  private final List<Projectile> trackedProjectiles = new ArrayList<>();
  private final BarrierService barrierService;
  private final BarriersConfig config;

  public ProjectileMovementHandler(BarriersConfig config, BarrierService barrierService) {
    super(20, 1, false);

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
  @Override
  public void run() {
    var iterator = trackedProjectiles.iterator();
    while (iterator.hasNext()) {
      Projectile projectile = iterator.next();
      if (projectile.isDead()) {
        iterator.remove();
        continue;
      } else if (projectile.isOnGround() || !projectile.getChunk().isLoaded()) {
        continue;
      }

      Location startLoc = LocationUtils.toBlockLoc(projectile.getLocation());
      Location endLoc = startLoc.clone().add(
          LocationUtils.toBlockLoc(projectile.getVelocity().toLocation(startLoc.getWorld()))
      );

      for (Location loc : LocationUtils.getBlocksArea(startLoc, endLoc)) {
        if (barrierService.findBarrierByBlock(loc.getBlock()).isEmpty()) {
          continue;
        }

        reflectProjectile(projectile);
        return;
      }
    }
  }

  private void reflectProjectile(Projectile projectile) {
    projectile.getWorld().playSound(
        projectile.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1
    );

    Vector velocity = projectile.getVelocity();
    boolean isXStationary = velocity.getX() > -0.07 && velocity.getX() < 0.07;
    boolean isZStationary = velocity.getZ() > -0.07 && velocity.getZ() < 0.07;

    if (isXStationary && isZStationary) {
      velocity.setX(-0.2);
    }

    projectile.setVelocity(velocity.multiply(-1).normalize());
  }

  /**
   * Forget projectiles if it hits an entity
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onProjectileHitEntity(ProjectileHitEvent event) {
    if (event.getHitEntity() == null) {
      return;
    }

    forgetProjectile(event.getEntity());
  }

  /**
   * Track launched projectiles
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onProjectileShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player) && config.shouldRepelOnlyPlayerProjectiles()) {
      return;
    }

    trackProjectile(event.getEntity());
  }

  public void trackProjectile(Projectile projectile) {
    trackedProjectiles.add(projectile);
  }

  public void forgetProjectile(Projectile projectile) {
    trackedProjectiles.remove(projectile);
  }

}
