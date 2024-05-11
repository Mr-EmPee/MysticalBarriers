package plugin;

import io.github.empee.easygui.EasyGUI;
import io.github.empee.lightwire.LightWire;
import org.bukkit.event.Listener;
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

  public static final String COMMAND = "mb";
  private final LightWire iocContainer = LightWire.of(getClass().getPackage());

  @Override
  public void onEnable() {
    EasyGUI.init(this);

    iocContainer.addComponent(this);
    iocContainer.addComponent(new BukkitSyncedExecutor(this));
    iocContainer.addComponent(new BukkitAsyncExecutor(this));

    iocContainer.load();
  }

  public void reload() {
    for (IReloadable reloadable : iocContainer.getInstances(IReloadable.class)) {
      reloadable.reload();
    }
  }

  @Override
  public void onDisable() {
    for (Closeable closeable : iocContainer.getInstances(Closeable.class)) {
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

}
