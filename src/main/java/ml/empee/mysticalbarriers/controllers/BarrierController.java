package ml.empee.mysticalbarriers.controllers;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.config.CommandsConfig;
import ml.empee.mysticalbarriers.constants.Permissions;
import ml.empee.mysticalbarriers.handlers.BarrierSelectionHandler;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.helpers.CuboidRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

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

    CuboidRegion selection = selectionHandler.getSelection(sender);
    if (!selection.isValid()) {
      Logger.log(sender, "&cYou need to make a selection!");
      return;
    }

    Barrier barrier = new Barrier(name);
    barrier.setRegion(selection.clone());
    selection.invalidate();

    barrierService.save(barrier);
    Logger.log(sender, "&7You created a barrier named &e%s", name);
  }

  /**
   * Delete a barrier
   */
  @CommandMethod("mb delete <barrier>")
  @CommandPermission(Permissions.ADMIN)
  public void deleteBarrier(Player sender, @Argument Barrier barrier) {
    barrierService.delete(barrier);
    Logger.log(sender, "&7You deleted the barrier &e%s", barrier.getId());
  }

  /**
   * Parse a barrier from using a command input
   */
  @Parser(suggestions = "barriersProvider")
  public Barrier parseBarrier(CommandContext<CommandSender> context, Queue<String> input) {
    String barrierID = input.peek();
    if (barrierID == null) {
      throw new IllegalArgumentException("Missing barrier name");
    }

    Optional<Barrier> barrier = barrierService.findById(barrierID);
    if (barrier.isPresent()) {
      input.remove();
      return barrier.get();
    }

    throw new IllegalArgumentException("Unable to find the barrier");
  }

  /**
   * Find all matchable barriers based on a command input
   */
  @Suggestions("barriersProvider")
  public List<String> getBarriersSuggestions(
      CommandContext<CommandSender> context, String input
  ) {
    return barrierService.findAll().stream()
        .map(Barrier::getId)
        .toList();
  }

}
