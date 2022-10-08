package ml.empee.mysticalBarriers.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.NoArgsConstructor;
import ml.empee.mysticalBarriers.utils.serialization.SerializationUtils;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class FileSaver {

  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(FileSaver.class);
  private static final List<FileSaveTask> tasks = new ArrayList<>();

  static {
    Bukkit.getPluginManager().registerEvents(new FileSaverListener(), plugin);
  }

  public static AtomicBoolean scheduleSaving(Object object, String target, Long saveInterval) {
    FileSaveTask task = new FileSaveTask(object, target);
    tasks.add(task);

    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, saveInterval, saveInterval);
    return task.savingScheduled;
  }

  public static AtomicBoolean scheduleSaving(Object object, String target) {
    return scheduleSaving(object, target, 20L * 120);
  }

  private static class FileSaveTask implements Runnable {

    private final AtomicBoolean savingScheduled = new AtomicBoolean(false);
    private final Object object;
    private final String target;

    public FileSaveTask(Object object, String target) {
      this.object = object;
      this.target = target;
    }

    @Override
    public synchronized void run() {
      if(savingScheduled.get()) {
        SerializationUtils.serialize(object, target);
        Logger.info("Saved file " + target);
        savingScheduled.set(false);
      }
    }

  }

  private static class FileSaverListener implements Listener {

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
      if (event.getPlugin().equals(plugin)) {
        tasks.forEach(FileSaveTask::run);
      }
    }

  }

}
