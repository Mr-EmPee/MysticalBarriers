package ml.empee.mysticalbarriers;

import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager;
import lombok.Getter;
import ml.empee.commandsManager.CommandManager;
import ml.empee.commandsManager.command.CommandExecutor;
import ml.empee.ioc.SimpleIoC;
import ml.empee.mysticalbarriers.utils.Metrics;
import ml.empee.notifier.Notifier;
import org.bukkit.plugin.java.JavaPlugin;

public final class MysticalBarriersPlugin extends JavaPlugin {

  public static final String PREFIX = "  &5MyB &8»&r ";
  private static final String SPIGOT_PLUGIN_ID = "105671";
  private static final Integer METRICS_PLUGIN_ID = 16669;

  static {
    CommandExecutor.setPrefix(PREFIX);
  }

  private final InventoryManager inventoryManager = new InventoryManager(this);
  private final CommandManager commandManager = new CommandManager(this);
  @Getter
  private final SimpleIoC iocContainer = new SimpleIoC(this);

  public void onEnable() {
    inventoryManager.invoke();

    iocContainer.addBean(commandManager);
    iocContainer.addBean(inventoryManager);

    iocContainer.initialize("relocations");

    Metrics.of(this, METRICS_PLUGIN_ID);
    Notifier.listenForUpdates(this, SPIGOT_PLUGIN_ID);
  }

  public void onDisable() {
    commandManager.unregisterCommands();
    iocContainer.removeAllBeans();
  }

  public void reload() {
    commandManager.unregisterCommands();
    iocContainer.removeAllBeans();
    iocContainer.initialize("relocations");
  }
}
