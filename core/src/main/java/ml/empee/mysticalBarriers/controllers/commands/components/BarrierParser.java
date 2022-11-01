package ml.empee.mysticalBarriers.controllers.commands.components;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import lombok.EqualsAndHashCode;
import ml.empee.commandsManager.parsers.ParameterParser;
import ml.empee.commandsManager.parsers.ParserDescription;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;

@EqualsAndHashCode(callSuper = true)
public class BarrierParser extends ParameterParser<Barrier> {

  private final BarriersService barriersService;

  public BarrierParser(BarriersService barriersService, String label, String defaultValue) {
    super(label, defaultValue);
    this.descriptor = new ParserDescription("barrier", "Thia parameter identify a barrier by it's name", null);
    this.barriersService = barriersService;
  }

  protected BarrierParser(BarrierParser parser) {
    super(parser);
    this.barriersService = parser.barriersService;
  }

  @Override
  public Barrier parse(int i, String... args) {
    Barrier result = barriersService.findBarrierByID(args[i]);
    if(result == null) {
      throw new CommandException("Can't find the barrier '&e" + args[i] + "&c'");
    }

    return result;
  }

  @Override
  public List<String> getSuggestions(CommandSender source, String arg) {
    return barriersService.findAllBarriers().stream()
        .map(Barrier::getId)
        .collect(Collectors.toList());
  }

  @Override
  public ParameterParser<Barrier> copyParser() {
    return new BarrierParser(this);
  }

}