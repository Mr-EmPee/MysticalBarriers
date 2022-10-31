package ml.empee.mysticalBarriers.utils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import lombok.NoArgsConstructor;
import ml.empee.mysticalBarriers.utils.serialization.SerializationUtils;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ScheduledFileSaver {

  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ScheduledFileSaver.class);
  private static final ArrayList<Task> tasks = new ArrayList<>();

  static {
    Bukkit.getPluginManager().registerEvents(new FileSaverListener(), plugin);
  }

  public static Task scheduleSaving(Object object, String target, Long saveInterval) {
    Task task = new Task(object, target);
    tasks.add(task);

    BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, saveInterval, saveInterval);
    task.taskId = bukkitTask.getTaskId();

    return task;
  }

  public static void unregisterTask(Task task) {
    Bukkit.getScheduler().cancelTask(task.taskId);
    tasks.remove(task);
  }

  public static class Task implements Runnable {

    private final Object object;
    private final String target;
    private final AtomicBoolean isDirty = new AtomicBoolean(false);
    private int taskId;

    private Task(Object object, String target) {
      this.object = object;
      this.target = target;
    }

    @Override
    public synchronized void run() {
      if (isDirty.get()) {
        SerializationUtils.serialize(object, target);
        Logger.info("Saved file " + target);
        isDirty.set(false);
      }
    }

    public void setDirty() {
      isDirty.set(true);
    }

  }

  private static class FileSaverListener implements Listener {

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
      if (event.getPlugin().equals(plugin)) {
        tasks.forEach(Task::run);
      }
    }

  }

}
