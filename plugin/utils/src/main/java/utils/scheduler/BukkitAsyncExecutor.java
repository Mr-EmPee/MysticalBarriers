package utils.scheduler;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executor;

/**
 * Executor that is going to schedule the task using the bukkit async scheduler
 */

@RequiredArgsConstructor
public class BukkitAsyncExecutor implements Executor {

  private final JavaPlugin plugin;

  @Override
  public void execute(Runnable command) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, command);
  }

}
