package ml.empee.mysticalbarriers.handlers;

import java.util.HashMap;
import java.util.List;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.ioc.ScheduledTask;
import ml.empee.mysticalbarriers.config.PluginConfig;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarriersService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import ml.empee.mysticalbarriers.utils.reflection.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class ProjectileBlocker extends ScheduledTask implements RegisteredListener, Bean {

  private final HashMap<Entity, Player> followedEntities = new HashMap<>();
  private final BarriersService barriersService;
  private final PluginConfig config;

  public ProjectileBlocker(PluginConfig config, BarriersService barriersService) {
    super(0, 1, false);

    this.config = config;
    this.barriersService = barriersService;
  }

  public void run() {
    followedEntities.entrySet().removeIf(
      entry -> reflectProjectileOnBarrierTouch(entry.getKey(), entry.getValue())
    );
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

}
