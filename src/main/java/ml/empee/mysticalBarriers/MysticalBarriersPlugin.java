package ml.empee.mysticalBarriers;

import ml.empee.commandsManager.CommandManager;
import ml.empee.commandsManager.command.CommandExecutor;
import ml.empee.commandsManager.parsers.ParserManager;
import ml.empee.ioc.SimpleIoC;
import ml.empee.mysticalBarriers.controllers.PluginController;
import ml.empee.mysticalBarriers.controllers.parsers.BarrierParser;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.MCLogger;
import ml.empee.mysticalBarriers.utils.Metrics;
import ml.empee.notifier.Notifier;
import org.bukkit.plugin.java.JavaPlugin;

public final class MysticalBarriersPlugin extends JavaPlugin {

  private static final String PREFIX = "  &5MyB &8»&r ";
  private static final String SPIGOT_PLUGIN_ID = "105671";
  private static final Integer METRICS_PLUGIN_ID = 16669;

  private SimpleIoC iocContainer;

  @Override
  public void onEnable() {
    MCLogger.setPrefix(PREFIX);

    iocContainer = SimpleIoC.initialize(this, "relocations");
    registerCommands();

    Metrics.of(this, METRICS_PLUGIN_ID);
    Notifier.listenForUpdates(this, SPIGOT_PLUGIN_ID);
  }

  public void reload() {
    iocContainer.removeAllBeans();
    iocContainer = SimpleIoC.initialize(this, "relocations");
    registerCommands();
  }

  private void registerCommands() {
    CommandExecutor.setPrefix(MCLogger.getPrefix());
    CommandManager commandManager = new CommandManager(this);
    ParserManager parserManager = commandManager.getParserManager();
    parserManager.registerParser(
        new BarrierParser(iocContainer.getBean(BarriersService.class)), null, Barrier.class
    );

    commandManager.registerCommand(iocContainer.getBean(PluginController.class));
  }
}
