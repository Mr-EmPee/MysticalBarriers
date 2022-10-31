package ml.empee.mysticalBarriers.services.listeners;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class AbstractListener implements Listener {

  public final void unregister() {
    HandlerList.unregisterAll(this);

    onUnregister();
  }

  protected void onUnregister() {}

}
