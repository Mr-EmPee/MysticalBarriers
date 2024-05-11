package plugin.handlers;

import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugin.model.Barrier;
import plugin.services.BarriersService;
import utils.regions.CubicRegion;

import java.util.ArrayList;
import java.util.List;

@LightWired
@RequiredArgsConstructor
public class BarrierEntitiesActionHandler extends BukkitRunnable implements Listener {

  private final List<Projectile> trackedProjectiles = new ArrayList<>();
  private final List<Item> trackedItems = new ArrayList<>();

  private final BarriersService barriersService;

  @EventHandler
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
    trackedProjectiles.add(event.getEntity());
  }

  @EventHandler
  public void onItemDrop(ItemSpawnEvent event) {
    trackedItems.add(event.getEntity());
  }

  @Override
  public void run() {
    computeBarrierProjectileCollisions();
    computeBarrierItemCollisions();
  }

  private void computeBarrierProjectileCollisions() {
    var iterator = trackedProjectiles.iterator();
    while (iterator.hasNext()) {
      var projectile = iterator.next();

      if (projectile.isOnGround()) {
        continue;
      }

      if (projectile.isDead()) {
        iterator.remove();
        continue;
      }

      var loc = projectile.getLocation();
      if (collidesWithBarrier(loc, projectile.getVelocity())) {
        iterator.remove();
        projectile.remove();

        loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
      }
    }
  }

  private void computeBarrierItemCollisions() {
    var iterator = trackedItems.iterator();
    while (iterator.hasNext()) {
      var item = iterator.next();

      if (item.isOnGround()) {
        continue;
      }

      if (item.isDead()) {
        iterator.remove();
        continue;
      }

      var loc = item.getLocation();
      if (collidesWithBarrier(loc, item.getVelocity())) {
        iterator.remove();

        item.teleport(item.getOrigin());
        item.setVelocity(new Vector());

        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
      }
    }
  }

  public boolean collidesWithBarrier(Location start, Vector velocity) {
    var movementBox = CubicRegion.of(start, start.clone().add(velocity));

    for (Barrier barrier : barriersService.findAll()) {
      if (barrier.findVisibleRegion(movementBox) != null) {
        return true;
      }
    }

    return false;
  }

}
