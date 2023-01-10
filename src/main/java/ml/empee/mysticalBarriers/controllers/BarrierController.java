package ml.empee.mysticalBarriers.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.empee.commandsManager.command.CommandNode;
import ml.empee.commandsManager.command.Controller;
import ml.empee.commandsManager.parsers.types.annotations.IntegerParam;
import ml.empee.ioc.annotations.Bean;
import ml.empee.mysticalBarriers.controllers.parsers.BarrierDirection;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.MCLogger;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerContext;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerData;
import ml.empee.mysticalBarriers.utils.nms.ServerVersion;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Bean
@RequiredArgsConstructor
public class BarrierController extends Controller {

  private final PlayerContext<Barrier> barrierCreationContext = PlayerContext.get("barrierCreationContext");
  private final BarriersService barriersService;

  @CommandNode(
      parent = "mb",
      label = "create",
      description = "Select the barrier corners by right-clicking on a block \n"
          + "or cancel the operation with a left click",
      permission = "mysticalbarriers.command.create"
  )
  public void createBarrier(Player sender, String name) {
    if (barriersService.findBarrierByID(name) != null) {
      MCLogger.error(sender, "A barrier named '&e%s&r' already exists!", name);
      return;
    }

    barrierCreationContext.put(PlayerData.of(sender, new Barrier(name)));
    MCLogger.info(sender,
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
  public void modifyBarrierMaterial(CommandSender sender, Barrier barrier, Material material) {
    barrier.setMaterial(material);
    barrier.setBlockData(null);

    barriersService.updateBarrier(barrier);
    MCLogger.info(sender, "Barrier material changed to '&e%s&r'", material.name());
  }

  @CommandNode(
      parent = "mb",
      label = "modify range",
      description = "Change the activation range of a barrier",
      permission = "mysticalbarriers.command.modify"
  )
  public void modifyBarrierRange(
      CommandSender sender, Barrier barrier,
      @IntegerParam(min = 0, max = 16) Integer range
  ) {
    barrier.setActivationRange(range);

    barriersService.updateBarrier(barrier);
    MCLogger.info(sender, "Barrier activation range changed to &e%d&r", range);
  }

  @CommandNode(
      parent = "mb",
      label = "modify direction",
      description = "Specify the connection direction of the barrier's blocks",
      permission = "mysticalbarriers.command.modify"
  )
  public void modifyBarrierDirection(CommandSender sender, Barrier barrier, BarrierDirection direction) {
    if (ServerVersion.isLowerThan(1, 13)) {
      MCLogger.error(sender, "This feature is available on 1.13+ servers only");
      return;
    }

    String blockData = direction.buildFacesData(barrier.getMaterial());
    if (blockData == null) {
      MCLogger.error(
          sender, "The direction '&e%s&r' isn't supported by '&e%s&r'",
          direction.name(), barrier.getMaterial()
      );
      return;
    }

    barrier.setBlockData(blockData);
    barriersService.updateBarrier(barrier);

    MCLogger.info(sender, "Barrier direction changed to '&e%s&r'", direction.name());
  }

  @CommandNode(
      parent = "mb",
      label = "list",
      description = "List all the barriers created",
      permission = "mysticalbarriers.command.list"
  )
  public void listBarriers(CommandSender sender) {
    List<Barrier> barriers = barriersService.findAllBarriers();

    StringBuilder message;
    if (barriers.isEmpty()) {
      message = new StringBuilder("There aren't any barriers created yet!");
    } else {
      message = new StringBuilder("\n\n");

      for (Barrier barrier : barriers) {
        message.append("\t- &e").append(barrier.getId()).append("\n");
      }
    }

    MCLogger.info(sender, message.toString());
  }

  @CommandNode(
      parent = "mb",
      label = "remove",
      description = "Remove a barrier",
      permission = "mysticalbarriers.command.remove"
  )
  public void removeBarrier(CommandSender sender, Barrier barrier) {
    if (barriersService.removeBarrier(barrier)) {
      MCLogger.info(sender, "The barrier '&e%s&r' has been removed!", barrier.getId());
    }
  }

}
