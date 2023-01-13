package ml.empee.mysticalBarriers.utils.helpers;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Setter @Accessors(chain = true, fluent = true, makeFinal = true)
public abstract class DynamicListener<T extends Event> {

  private static JavaPlugin plugin = JavaPlugin.getProvidingPlugin(DynamicListener.class);
  private Listener listener;
  private Class<? extends Event> eventClass;
  private EventPriority priority = EventPriority.NORMAL;
  private boolean ignoreCancelled = false;

  public abstract void onEvent(T event);

  public void register() {
    listener = new Listener() {};
    plugin.getServer().getPluginManager().registerEvent(
        eventClass,
        listener,
        priority,
        (l, e) -> onEvent((T) e),
        plugin,
        ignoreCancelled
    );
  }

}
