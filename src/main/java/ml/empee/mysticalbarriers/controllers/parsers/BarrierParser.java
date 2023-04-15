package ml.empee.mysticalbarriers.controllers.parsers;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.config.CommandsConfig;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Parse barriers
 */

@RequiredArgsConstructor
public class BarrierParser implements Bean, ArgumentParser<CommandSender, Barrier> {

  private final CommandsConfig commandsConfig;
  private final BarrierService barrierService;

  @Override
  public void onStart() {
    commandsConfig.register(Barrier.class, this);
  }

  @Override
  public ArgumentParseResult<Barrier> parse(
      CommandContext<CommandSender> commandContext, Queue<String> inputQueue
  ) {
    String input = inputQueue.peek();
    if (input == null) {
      return ArgumentParseResult.failure(
          new IllegalArgumentException("Missing barrier name")
      );
    }

    Optional<Barrier> barrier = barrierService.findById(input);
    if (barrier.isPresent()) {
      inputQueue.remove();
      return ArgumentParseResult.success(barrier.get());
    }

    return ArgumentParseResult.failure(
        new IllegalArgumentException("Unable to find the base type")
    );
  }

  @Override
  public List<String> suggestions(
      CommandContext<CommandSender> commandContext, String input
  ) {
    return barrierService.findAll().stream()
        .map(Barrier::getId)
        .toList();
  }

}
