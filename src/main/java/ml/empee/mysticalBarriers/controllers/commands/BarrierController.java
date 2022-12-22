package ml.empee.mysticalBarriers.controllers.commands;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import ml.empee.commandsManager.command.Command;
import ml.empee.commandsManager.command.annotations.CommandNode;
import ml.empee.commandsManager.command.annotations.CommandRoot;
import ml.empee.commandsManager.parsers.types.annotations.IntegerParam;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.commands.listeners.BarrierDefiner;
import ml.empee.mysticalBarriers.controllers.commands.parsers.BarrierDirection;
import ml.empee.mysticalBarriers.listeners.BarrierSpawner;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerContext;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.Logger;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerData;
import ml.empee.mysticalBarriers.utils.nms.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@CommandRoot(label = "mb", aliases = { "mysticalbarriers", "mysticalb" })
public class BarrierController extends Command {

  private final PlayerContext<Barrier> barrierCreationContext = PlayerContext.get("barrierCreationContext");
  private final BarriersService barriersService;
  private final BarrierSpawner barrierSpawner;

  public BarrierController(BarriersService barriersService, BarrierSpawner barrierSpawner) {
    this.barriersService = barriersService;
    this.barrierSpawner = barrierSpawner;

    registerListeners(
        new BarrierDefiner(barriersService)
    );
  }

  @CommandNode(
      parent = "mb",
      label = "help",
      description = "Send the help menu",
      permission = "mysticalbarriers.command.help"
  )
  public void sendHelpMenu(CommandSender sender, @IntegerParam(min = 1, defaultValue = "1") Integer page) {
    getHelpMenu().sendHelpMenu(sender, page);
  }

  @CommandNode(
      parent = "mb",
      label = "create",
      description = "Select the barrier corners by right-clicking on a block \n"
                    + "or cancel the operation with a left click",
      permission = "mysticalbarriers.command.create"
  )
  public void onBarrierCreate(Player sender, String name) {
    if (barriersService.findBarrierByID(name) != null) {
      Logger.error(sender, "A barrier named '&e%s&r' already exists!", name);
      return;
    }

    barrierCreationContext.put(PlayerData.of(sender, new Barrier(name)));
    Logger.info(sender,
        "Barrier creation mode enabled! \n\n"
        + "\tSelect the barrier corners by right-clicking on a block \n"
        + "\tor cancel the operation with a left click \n"
    );
  }

  @CommandNode(
      parent = "mb",
      label = "modify material",
      description = "Change the material of a barrier",
      permission = "mysticalbarriers.command.modify"
  )
  public void onBarrierModifyMaterial(CommandSender sender, Barrier barrier, Material material) {
    barrier.setMaterial(material);
    barrier.setBlockData(null);

    barriersService.updateBarrier(barrier);
    Logger.info(sender, "Barrier material changed to '&e%s&r'", material.name());
  }

  @CommandNode(
      parent = "mb",
      label = "modify range",
      description = "Change the activation range of a barrier",
      permission = "mysticalbarriers.command.modify"
  )
  public void onBarrierModifyRange(
      CommandSender sender, Barrier barrier,
      @IntegerParam(min = 0, max = 16) Integer range
  ) {
    barrier.setActivationRange(range);

    barriersService.updateBarrier(barrier);
    Logger.info(sender, "Barrier activation range changed to &e%d&r", range);
  }

  @CommandNode(
      parent = "mb",
      label = "modify direction",
      description = "Specify the connection direction of the barrier's blocks",
      permission = "mysticalbarriers.command.modify"
  )
  public void onBarrierModifyConnectionDirection(CommandSender sender, Barrier barrier, BarrierDirection direction) {
    if(ServerVersion.isLowerThan(1, 13)) {
      Logger.error(sender, "This feature is available on 1.13+ servers only");
      return;
    }

    String blockData = direction.buildFacesData(barrier.getMaterial());
    if(blockData == null) {
      Logger.error(
          sender, "The direction '&e%s&r' isn't supported by '&e%s&r'",
          direction.name(), barrier.getMaterial()
      );
      return;
    }

    barrier.setBlockData(blockData);
    barriersService.updateBarrier(barrier);

    Logger.info(sender, "Barrier direction changed to '&e%s&r'", direction.name());
  }

  @CommandNode(
      parent = "mb",
      label = "list",
      description = "List all the barriers created",
      permission = "mysticalbarriers.command.list"
  )
  public void onBarrierList(CommandSender sender) {
    Set<Barrier> barriers = barriersService.findAllBarriers();

    StringBuilder message;
    if (barriers.isEmpty()) {
      message = new StringBuilder("There aren't any barriers created yet!");
    } else {
      message = new StringBuilder("\n\n");

      for (Barrier barrier : barriers) {
        message.append("\t- &e").append(barrier.getId()).append("\n");
      }
    }

    Logger.info(sender, message.toString());
  }

  @CommandNode(
      parent = "mb",
      label = "remove",
      description = "Remove a barrier",
      permission = "mysticalbarriers.command.remove"
  )
  public void onBarrierRemove(CommandSender sender, Barrier barrier) {
    if (barriersService.removeBarrier(barrier)) {
      Logger.info(sender, "The barrier '&e%s&r' has been removed!", barrier.getId());
    }
  }

  @CommandNode(
      parent = "mb",
      label = "performance",
      description = "Use this command to check the performance of the plugin\n" +
      "&c&lPS. &cHigh range number could crash the server!",
      permission = "mysticalbarriers.command.debug"
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

    Logger.info(sender,
        "More then &c1 tick is LAG&r! Remember that this is a test, not a real scenario " +
            "in a real scenario everything would be &cmultiplied&r by the number of player inside " +
            "a barrier range and the number of barriers with overlapping ranges\n\n"
            + "  Blocks to send: &e%d\n"
            + "  Time: &e%dms\n"
            + "  Ticks: &e%.2f\n", blocks.get(), time, ((double) time/100)
    );
  }

  @CommandNode(
      parent = "mb",
      label = "debug",
      description = "Use this command if you're having issues with the plugin\n"
                    + "and you want to send the plugin's debug information to the developer",
      permission = "mysticalbarriers.command.debug"
  )
  public void onDebug(CommandSender sender) {
    if (!Logger.isDebugEnabled()) {
      Logger.setDebugEnabled(true);
      Logger.info(sender, "Debug mode enabled!");
    } else {
      Logger.setDebugEnabled(false);
      Logger.info(sender, "Debug mode disabled!");
    }
  }

  @CommandNode(
      parent = "mb",
      label = "reload",
      description = "Reload the plugin",
      permission = "mysticalbarriers.command.reload"
  )
  public void onReload(CommandSender sender) {
    JavaPlugin.getPlugin(MysticalBarriersPlugin.class).reload();
    Logger.info(sender, "The plugin has been reloaded!");
  }

}
