package plugin.handlers;

import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
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

  private final List<Entity> trackedEntities = new ArrayList<>();

  private final BarriersService barriersService;

  @EventHandler
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
    trackedEntities.add(event.getEntity());
  }

  @EventHandler
  public void onItemDrop(ItemSpawnEvent event) {
    trackedEntities.add(event.getEntity());
  }

  public void onProjectileCollide(Projectile projectile) {
    projectile.setVelocity(projectile.getVelocity().multiply(-1));

    var loc = projectile.getLocation();
    loc.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1);
  }

  public void onItemCollide(Item item) {
    var loc = item.getLocation();

    item.teleport(item.getOrigin());
    item.setVelocity(new Vector());

    loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
  }

  @Override
  public void run() {
    var iterator = trackedEntities.iterator();
    while (iterator.hasNext()) {
      var entity = iterator.next();

      if (entity.isOnGround()) {
        continue;
      }

      if (entity.isDead()) {
        iterator.remove();
        continue;
      }

      var loc = entity.getLocation();
      if (collidesWithBarrier(loc, entity.getVelocity())) {
        iterator.remove();

        if (entity instanceof Projectile) {
          onProjectileCollide((Projectile) entity);
        } else if (entity instanceof Item) {
          onItemCollide((Item) entity);
        }
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
