package ml.empee.mysticalbarriers.controllers;

import lombok.RequiredArgsConstructor;
import ml.empee.commandsManager.command.Controller;
import ml.empee.commandsManager.command.annotations.CommandNode;
import ml.empee.commandsManager.command.annotations.Context;
import ml.empee.commandsManager.parsers.types.annotations.IntegerParam;
import ml.empee.commandsManager.parsers.types.annotations.MaterialParam;
import ml.empee.ioc.Bean;
import ml.empee.ioc.annotations.DependsOn;
import ml.empee.mysticalbarriers.constants.PluginItems;
import ml.empee.mysticalbarriers.controllers.parsers.BarrierDirection;
import ml.empee.mysticalbarriers.controllers.parsers.BarrierParser;
import ml.empee.mysticalbarriers.controllers.views.BarrierEditingGUI;
import ml.empee.mysticalbarriers.controllers.views.BarriersListGUI;
import ml.empee.mysticalbarriers.handlers.BarrierDefiner;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarriersService;
import ml.empee.mysticalbarriers.utils.helpers.Logger;
import ml.empee.mysticalbarriers.utils.reflection.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@DependsOn(BarrierParser.class)
public class BarrierController extends Controller implements Bean {

  private final Logger logger;
  private final BarrierDefiner barrierDefiner;
  private final BarriersService barriersService;
  private final PluginItems pluginItems;

  @CommandNode(
    parent = "mb",
    label = "create",
    description = "Use the wand to create a barrier"
  )
  public void createBarrier(Player sender, String name) {
    if (barriersService.findBarrierByID(name) != null) {
      logger.error(sender, "A barrier named '&e%s&r' already exists!", name);
      return;
    }

    Location[] corners = barrierDefiner.getSelectedCorners(sender);
    if (corners[0] == null || corners[1] == null) {
      logger.error(sender, "You need to select two corners!");
      return;
    }

    Barrier barrier = new Barrier(name);
    barrier.setCorners(corners[0], corners[1]);

    barriersService.saveBarrier(barrier);
    barrierDefiner.clearSelectedCorners(sender);

    logger.info(sender, "The barrier &e%s &rhas been created!", name);
  }

  @CommandNode(
    parent = "mb",
    label = "wand",
    description = "Gives you the barrier wand"
  )
  public void giveRegionWand(Player sender) {
    sender.getInventory().addItem(
      pluginItems.getSelectionWand().build()
    );

    logger.info(sender, "You have been given the barrier wand!");
  }

  @CommandNode(
    parent = "mb",
    label = "modify",
    description = "Open the barrier modify menu"
  )
  public void openBarrierModifyMenu(Player sender, Barrier barrier) {
    new BarrierEditingGUI(barrier).open(sender);
  }

  @CommandNode(
    parent = "modify",
    label = "name",
    description = "Change the barrier name"
  )
  public void modifyBarrierName(CommandSender sender, @Context Barrier barrier, String name) {
    if (name.trim().isEmpty()) {
      logger.error(sender, "The barrier name isn't valid!");
      return;
    } else if (barriersService.findBarrierByID(name) != null) {
      logger.error(sender, "A barrier named '&e%s&r' already exists!", name);
      return;
    }

    barrier.setId(name);
    barriersService.updateBarrier(barrier);
    logger.info(sender, "Barrier name changed to '&e%s&r'", name);
  }

  @CommandNode(
    parent = "modify",
    label = "corners",
    description = "Change the barrier corners"
  )
  public void modifyBarrierCorners(Player sender, @Context Barrier barrier) {
    Location[] corners = barrierDefiner.getSelectedCorners(sender);
    if (corners[0] == null || corners[1] == null) {
      logger.error(sender, "You need to select two corners!");
      return;
    }

    barriersService.hideBarrier(barrier);
    barrier.setCorners(corners[0], corners[1]);
    barriersService.updateBarrier(barrier);

    barrierDefiner.clearSelectedCorners(sender);
    logger.info(sender, "The corners of &e%s &rhave been changed!", barrier.getId());
  }

  @CommandNode(
    parent = "modify",
    label = "material",
    description = "Change the material name"
  )
  public void modifyBarrierMaterial(
    CommandSender sender, @Context Barrier barrier, @MaterialParam(onlyBlocks = true) Material material
  ) {
    barrier.setMaterial(material);
    barrier.setBlockData(null);

    barriersService.updateBarrier(barrier);
    logger.info(sender, "Barrier material changed to '&e%s&r'", material.name());
  }

  @CommandNode(
    parent = "modify",
    label = "range",
    description = "Change the barrier activation range"
  )
  public void modifyBarrierRange(
    CommandSender sender, @Context Barrier barrier,
    @IntegerParam(min = 0, max = 16) Integer range
  ) {

    barriersService.hideBarrier(barrier);
    barrier.setActivationRange(range);
    barriersService.updateBarrier(barrier);
    logger.info(sender, "Barrier activation range changed to &e%d&r", range);
  }

  @CommandNode(
    parent = "modify",
    label = "direction",
    description = "Change the barrier direction"
  )
  public void modifyBarrierDirection(
    CommandSender sender, @Context Barrier barrier, BarrierDirection direction
  ) {
    if (ServerVersion.isLowerThan(1, 13)) {
      logger.error(sender, "This feature is available on 1.13+ servers only");
      return;
    }

    BlockData blockData = direction.buildFacesData(barrier.getMaterial());
    if (blockData == null) {
      logger.error(
        sender, "The direction '&e%s&r' isn't supported by '&e%s&r'",
        direction.name(), barrier.getMaterial()
      );
      return;
    }

    barrier.setBlockData(blockData);
    barriersService.updateBarrier(barrier);

    logger.info(sender, "Barrier direction changed to '&e%s&r'", direction.name());
  }

  @CommandNode(
    parent = "mb",
    label = "list",
    description = "Open the barriers list menu"
  )
  public void showBarriersList(CommandSender sender) {
    if (sender instanceof Player) {
      new BarriersListGUI().open((Player) sender);
    } else {
      logger.info("Barriers list:");
      for (Barrier barrier : barriersService.findAllBarriers()) {
        logger.info(" - %s", barrier.getId());
      }
    }
  }

  @CommandNode(
    parent = "mb",
    label = "remove",
    description = "Remove a barrier"
  )
  public void removeBarrier(CommandSender sender, Barrier barrier) {
    if (barriersService.removeBarrier(barrier)) {
      logger.info(sender, "The barrier '&e%s&r' has been removed!", barrier.getId());
    }
  }

}
