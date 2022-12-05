package ml.empee.mysticalBarriers.listeners;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractListener implements Listener {

  protected final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(AbstractListener.class);

  public final void unregister() {
    HandlerList.unregisterAll(this);

    onUnregister();
  }

  protected void onUnregister() {}

}
