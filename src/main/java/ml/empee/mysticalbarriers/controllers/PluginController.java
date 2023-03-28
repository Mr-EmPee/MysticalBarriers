package ml.empee.mysticalbarriers.controllers;

import java.util.concurrent.atomic.AtomicInteger;
import ml.empee.commandsManager.CommandManager;
import ml.empee.commandsManager.command.CommandExecutor;
import ml.empee.commandsManager.command.annotations.CommandNode;
import ml.empee.commandsManager.parsers.types.annotations.IntegerParam;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.MysticalBarriersPlugin;
import ml.empee.mysticalbarriers.Permissions;
import ml.empee.mysticalbarriers.handlers.BarrierSpawner;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.utils.helpers.Logger;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandNode(
  label = "mb", aliases = {"mysticalbarriers", "mysticalb"},
  permission = Permissions.ADMIN_PERMISSION,
  exitNode = false
)

public class PluginController extends CommandExecutor implements Bean {

  private final MysticalBarriersPlugin plugin;
  private final Logger logger;
  private final BarrierSpawner barrierSpawner;

  public PluginController(
    MysticalBarriersPlugin plugin, CommandManager commandManager, Logger logger,
    BarrierController barrierController, BarrierSpawner barrierSpawner
  ) {
    this.plugin = plugin;
    this.logger = logger;
    this.barrierSpawner = barrierSpawner;

    addSubController(barrierController);
    commandManager.registerCommand(this);
  }

  @CommandNode(
    parent = "mb",
    label = "help",
    description = "Send the help menu"
  )
  public void sendHelpMenu(CommandSender sender, @IntegerParam(min = 1, defaultValue = "1") Integer page) {
    getHelpMenu().sendHelpMenu(sender, page);
  }

  @CommandNode(
    parent = "mb",
    label = "performance",
    description = "Use this command to check the performance of the plugin\n\n"
      + "&c&lPS. &cHigh range number could crash the server!"
  )
  public void onPerformanceTest(Player sender, @IntegerParam(max = 50, min = 1) Integer range) {
    AtomicInteger blocks = new AtomicInteger(0);
    Location location = sender.getLocation();

    Barrier barrier = new Barrier("performanceTest");
    barrier.setActivationRange(range);
    barrier.setCorners(
      new Location(location.getWorld(), 0, 0, 0),
      new Location(location.getWorld(), 100, 200, 100)
    );

    long time = System.currentTimeMillis();
    barrierSpawner.sendBarrierBlocks(sender, barrier,
      new Location(location.getWorld(), 50, 50, 50),
      new Location(location.getWorld(), 50, 150, 50)
    );
    time = (System.currentTimeMillis() - time) * 2;

    logger.info(sender,
      "More then &c1 tick is LAG&r! Remember that this is a test, not a real scenario " +
        "in a real scenario everything would be &cmultiplied&r by the number of player inside " +
        "a barrier range and the number of barriers with overlapping ranges\n\n"
        + "  Blocks to send: &e%d\n"
        + "  Time: &e%dms\n"
        + "  Ticks: &e%.2f\n", blocks.get(), time, ((double) time / 100)
    );
  }

  @CommandNode(
    parent = "mb",
    label = "debug",
    description = "Use this command if you're having issues with the plugin\n"
      + "and you want to send the plugin's debug information to the developer"
  )
  public void onDebug(CommandSender sender) {
    if (!logger.isDebugEnabled()) {
      logger.setDebugEnabled(true);
      logger.info(sender, "Debug mode enabled!");
    } else {
      logger.setDebugEnabled(false);
      logger.info(sender, "Debug mode disabled!");
    }
  }

  @CommandNode(
    parent = "mb",
    label = "reload",
    description = "Reload the plugin"
  )
  public void onReload(CommandSender sender) {
    plugin.initialize();
    logger.info(sender, "The plugin has been reloaded!");
  }

}
