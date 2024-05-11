package core.configs.server;

import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import utils.Messenger;

import java.util.List;

@LightWired
public class SchedulersConfig {


  public SchedulersConfig(JavaPlugin plugin, List<BukkitRunnable> schedulers) {
    for (BukkitRunnable scheduler : schedulers) {
      scheduler.runTaskTimer(plugin, 0, 1);
    }

    Messenger.log("Scheduler registered {}", schedulers.size());
  }

}
