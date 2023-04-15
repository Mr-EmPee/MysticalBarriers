package ml.empee.mysticalbarriers.model.content;

import ml.empee.ioc.ScheduledTask;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handle projectile movement
 */

public abstract class EntityTracker<T extends Entity> extends ScheduledTask {

  private final List<T> entities = new ArrayList<>();

  public EntityTracker() {
    super(0, 1, false);
  }

  /**
   * Repel any tracked projectile that will hit a barrier block
   */
  @Override
  public void run() {
    Iterator<T> iterator = entities.iterator();
    while (iterator.hasNext()) {
      T entity = iterator.next();

      if (entity.isDead() || !entity.isValid()) {
        iterator.remove();
        continue;
      }

      if (run(entity)) {
        iterator.remove();
      }
    }
  }

  /**
   * @return true if the entity doesn't need to be tracked anymore
   */
  protected abstract boolean run(T entity);

  public void trackEntity(T entity) {
    entities.add(entity);
  }

  public void forgetEntity(T entity) {
    entities.remove(entity);
  }

}
