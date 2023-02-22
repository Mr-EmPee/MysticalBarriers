package ml.empee.mysticalbarriers.controllers.parsers;

import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import ml.empee.commandsManager.CommandManager;
import ml.empee.commandsManager.parsers.DescriptionBuilder;
import ml.empee.commandsManager.parsers.ParameterParser;
import ml.empee.ioc.Bean;
import ml.empee.ioc.annotations.InversionOfControl;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarriersService;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

@EqualsAndHashCode(callSuper = true)
public class BarrierParser extends ParameterParser<Barrier> implements Bean {

  private final BarriersService barriersService;

  @InversionOfControl
  public BarrierParser(CommandManager commandManager, BarriersService barriersService) {
    this(barriersService);

    commandManager.getParserManager().registerParser(this, null, Barrier.class);
  }

  public BarrierParser(BarriersService barriersService) {
    this.barriersService = barriersService;
    this.label = "barrier";
  }

  @Override
  public DescriptionBuilder getDescriptionBuilder() {
    return new DescriptionBuilder("barrier", "This parameter identify a barrier by it's name");
  }

  @Override
  public Barrier parse(int i, String... args) {
    Barrier result = barriersService.findBarrierByID(args[i]);
    if (result == null) {
      throw new CommandException("Can't find the barrier '&e" + args[i] + "&c'");
    }

    return result;
  }

  @Override
  public List<String> buildSuggestions(CommandSender source, String arg) {
    return barriersService.findAllBarriers().stream()
      .map(Barrier::getId)
      .collect(Collectors.toList());
  }

  @Override
  public ParameterParser<Barrier> copyParser() {
    return new BarrierParser(barriersService);
  }

}
