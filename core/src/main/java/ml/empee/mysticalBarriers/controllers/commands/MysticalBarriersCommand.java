package ml.empee.mysticalBarriers.controllers.commands;

import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ml.empee.commandsManager.command.Command;
import ml.empee.commandsManager.command.annotations.CommandNode;
import ml.empee.commandsManager.command.annotations.CommandRoot;
import ml.empee.commandsManager.parsers.types.annotations.IntegerParam;
import ml.empee.commandsManager.parsers.types.annotations.StringParam;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.commands.components.BarrierDirection;
import ml.empee.mysticalBarriers.controllers.commands.listeners.BarrierDefiner;
import ml.empee.mysticalBarriers.helpers.PlayerContext;
import ml.empee.mysticalBarriers.helpers.Tuple;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.Logger;
import ml.empee.mysticalBarriers.utils.ServerVersion;

@CommandRoot(label = "mysticalbarriers", aliases = { "mb", "mysticalb" })
public class MysticalBarriersCommand extends Command {

  private final PlayerContext<Tuple<String, Location>> barrierCreationContext = PlayerContext.get("barrierCreation");
  private final BarriersService barriersService;

  public MysticalBarriersCommand(BarriersService barriersService) {
    this.barriersService = barriersService;

    registerListeners(
        new BarrierDefiner(barriersService)
    );
  }

  @CommandNode(
      parent = "mysticalbarriers",
      label = "create",
      description = "Select the barrier corners by right-clicking on a block \n"
                    + "or cancel the operation with a left click",
      permission = "mysticalbarriers.command.create"
  )
  public void onBarrierCreate(Player sender, @StringParam(label = "name") String barrier) {
    if (barriersService.findBarrierByID(barrier) != null) {
      Logger.error(sender, "A barrier named '&e%s&r' already exists!", barrier);
      return;
    }

    barrierCreationContext.put(sender, new Tuple<>(barrier, null));
    Logger.info(sender,
        "Barrier creation mode enabled! \n\n"
        + "\tSelect the barrier corners by right-clicking on a block \n"
        + "\tor cancel the operation with a left click \n"
    );
  }

  @CommandNode(
      parent = "mysticalbarriers",
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
      parent = "mysticalbarriers",
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
      parent = "mysticalbarriers",
      label = "modify direction",
      description = "Specify the connection direction of the barrier's blocks",
      permission = "mysticalbarriers.command.modify"
  )
  public void onBarrierModifyConnectionDirection(CommandSender sender, Barrier barrier, BarrierDirection barrierDirection) {
    if(ServerVersion.isLowerThan(1, 13)) {
      Logger.error(sender, "This feature is available on 1.13+ servers only");
      return;
    }

    try {
      barrier.getMaterial().createBlockData(barrierDirection.getData());
    } catch (IllegalArgumentException e) {
      Logger.error(sender, "The barrier material doesn't support the direction '&e%s&r'", barrierDirection.name());
      return;
    }

    barrier.setBlockData(barrierDirection.getData());
    barriersService.updateBarrier(barrier);

    Logger.info(sender, "Barrier direction changed to '&e%s&r'", barrierDirection.name());
  }

  @CommandNode(
      parent = "mysticalbarriers",
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
      parent = "mysticalbarriers",
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
      parent = "mysticalbarriers",
      label = "debug",
      permission = "mysticalbarriers.command.debug"
  )
  public void onDebug(CommandSender sender) {
    if (Logger.getLevel() != Level.FINE) {
      Logger.setLevel(Level.FINE);
      Logger.info(sender, "Debug mode enabled!");
    } else {
      Logger.setLevel(Level.INFO);
      Logger.info(sender, "Debug mode disabled!");
    }
  }

  @CommandNode(
      parent = "mysticalbarriers",
      label = "reload",
      permission = "mysticalbarriers.command.reload"
  )
  public void onReload(CommandSender sender) {
    JavaPlugin.getPlugin(MysticalBarriersPlugin.class).reload();
    Logger.info(sender, "The plugin has been reloaded!");
  }

}
