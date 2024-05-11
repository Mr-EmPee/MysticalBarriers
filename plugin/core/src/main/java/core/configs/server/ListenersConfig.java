package core.configs.server;

import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import utils.Messenger;

import java.util.List;

@LightWired
public class ListenersConfig {

  public ListenersConfig(JavaPlugin plugin, List<Listener> listeners) {
    var pm = plugin.getServer().getPluginManager();
    for (var listener : listeners) {
      pm.registerEvents(listener, plugin);
    }

    Messenger.log("Registered {} listeners", listeners.size());
  }

}
