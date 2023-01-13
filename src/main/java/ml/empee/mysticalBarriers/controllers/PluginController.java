package ml.empee.mysticalBarriers.controllers;

import java.util.concurrent.atomic.AtomicInteger;
import ml.empee.commandsManager.command.CommandExecutor;
import ml.empee.commandsManager.command.annotations.CommandNode;
import ml.empee.commandsManager.parsers.types.annotations.IntegerParam;
import ml.empee.ioc.Stoppable;
import ml.empee.ioc.annotations.Bean;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.listeners.BarrierSpawner;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.Permissions;
import ml.empee.mysticalBarriers.utils.MCLogger;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandNode(
    label = "mb", aliases = {"mysticalbarriers", "mysticalb"}, exitNode = false, permission = Permissions.ADMIN_PERMISSION
)

@Bean
public class PluginController extends CommandExecutor implements Stoppable {

  private final MysticalBarriersPlugin plugin;
  private final BarrierSpawner barrierSpawner;

  public PluginController(
      MysticalBarriersPlugin plugin, BarrierController barrierController, BarrierSpawner barrierSpawner
  ) {
    this.plugin = plugin;
    this.barrierSpawner = barrierSpawner;

    addSubController(barrierController);
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
    barrier.setFirstCorner(new Location(location.getWorld(), 0, 0, 0));
    barrier.setSecondCorner(new Location(location.getWorld(), 100, 200, 100));

    long time = System.currentTimeMillis();
    barrierSpawner.sendBarrierBlocks(sender, barrier,
        new Location(location.getWorld(), 50, 50, 50),
        new Location(location.getWorld(), 50, 150, 50)
    );
    time = (System.currentTimeMillis() - time) * 2;

    MCLogger.info(sender,
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
    if (!MCLogger.isDebugEnabled()) {
      MCLogger.setDebugEnabled(true);
      MCLogger.info(sender, "Debug mode enabled!");
    } else {
      MCLogger.setDebugEnabled(false);
      MCLogger.info(sender, "Debug mode disabled!");
    }
  }

  @CommandNode(
      parent = "mb",
      label = "reload",
      description = "Reload the plugin"
  )
  public void onReload(CommandSender sender) {
    plugin.reload();
    MCLogger.info(sender, "The plugin has been reloaded!");
  }

  @Override
  public void stop() {
    unregister();
  }
}
