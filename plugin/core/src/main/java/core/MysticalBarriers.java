package core;

import io.github.empee.easygui.EasyGUI;
import io.github.empee.lightwire.LightWire;
import org.bukkit.plugin.java.JavaPlugin;
import utils.IReloadable;
import utils.Messenger;
import utils.scheduler.BukkitAsyncExecutor;
import utils.scheduler.BukkitSyncedExecutor;

import java.io.Closeable;

/**
 * This is the bootstrap class of the plugin used for making the
 * necessary configuration in order to make it work correctly
 */

public class MysticalBarriers extends JavaPlugin {

  private static LightWire IOC;

  @Override
  public void onEnable() {
    EasyGUI.init(this);

    IOC = LightWire.of(getClass().getPackage());
    IOC.addComponent(this);
    IOC.addComponent(new BukkitSyncedExecutor(this));
    IOC.addComponent(new BukkitAsyncExecutor(this));

    IOC.load();
  }

  public void reload() {
    for (IReloadable reloadable : IOC.getInstances(IReloadable.class)) {
      reloadable.reload();
    }
  }

  @Override
  public void onDisable() {
    for (Closeable closeable : IOC.getInstances(Closeable.class)) {
      try {
        closeable.close();
      } catch (Exception e) {
        Messenger.error("Error while closing " + closeable.getClass(), e);
      }
    }
  }

  /**
   * @return true if the plugin is installed on a develop environment
   */
  public boolean isDevelop() {
    return getDescription().getVersion().endsWith("-SNAPSHOT");
  }

  public static <T> T getInstance(Class<T> clazz) {
    return IOC.getInstances(clazz).get(0);
  }

}
