package utils.scheduler;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executor;

/**
 * Executor that is going to schedule the task on the next tick
 */

@RequiredArgsConstructor
public class BukkitSyncedExecutor implements Executor {

  private final JavaPlugin plugin;

  @Override
  public void execute(Runnable command) {
    Bukkit.getScheduler().runTask(plugin, command);
  }

}
