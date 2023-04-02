package ml.empee.mysticalbarriers.controllers;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.config.CommandsConfig;
import ml.empee.mysticalbarriers.constants.Permissions;
import ml.empee.mysticalbarriers.handlers.BarrierSelectionHandler;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.helpers.CuboidSelection;
import org.bukkit.entity.Player;

/**
 * Controller used for managing barriers
 */

@RequiredArgsConstructor
public class BarrierController implements Bean {

  private final CommandsConfig commandsConfig;
  private final BarrierSelectionHandler selectionHandler;
  private final BarrierService barrierService;

  @Override
  public void onStart() {
    commandsConfig.register(this);
  }

  /**
   * Create a barrier from the player selection
   */
  @CommandMethod("mb create <name>")
  @CommandPermission(Permissions.ADMIN)
  public void createBarrier(Player sender, @Argument String name) {
    if (barrierService.findById(name).isPresent()) {
      Logger.log(sender, "&cA barrier named &e%s &calready exists", name);
      return;
    }

    CuboidSelection selection = selectionHandler.getSelection(sender);
    if (!selection.isValid()) {
      Logger.log(sender, "&cYou need to make a selection!");
      return;
    }

    Barrier barrier = new Barrier(name);
    barrier.setCorners(selection.getStart(), selection.getEnd());
    selection.invalidate();

    barrierService.save(barrier);
    Logger.log(sender, "&7You created a barrier named &e%s", name);
  }

}
