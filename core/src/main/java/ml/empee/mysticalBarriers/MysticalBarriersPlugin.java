package ml.empee.mysticalBarriers;

import ml.empee.commandsManager.command.Command;
import ml.empee.commandsManager.parsers.ParserManager;
import ml.empee.mysticalBarriers.controllers.commands.MysticalBarriersCommand;
import ml.empee.mysticalBarriers.controllers.commands.parsers.BarrierParser;
import ml.empee.mysticalBarriers.listeners.AbstractListener;
import ml.empee.mysticalBarriers.listeners.BarrierGuarder;
import ml.empee.mysticalBarriers.listeners.BarrierRefresher;
import ml.empee.mysticalBarriers.listeners.BarrierSpawner;
import ml.empee.mysticalBarriers.listeners.PlayerBlocker;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.AbstractService;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.MCLogger;
import ml.empee.notifier.SimpleNotifier;

public final class MysticalBarriersPlugin extends AbstractPlugin {

  @Override
  public void onEnable() {
    MCLogger.setPrefix("  &5MyB &8»&r ");
    Command.setPrefix(MCLogger.getPrefix());

    registerAll();

    new Metrics(this, 16669);
    SimpleNotifier.scheduleNotifier("105671", this, 1L);
  }

  @Override
  protected Command[] buildCommands() {
    return new Command[] {
        new MysticalBarriersCommand(getService(BarriersService.class))
    };
  }

  @Override
  protected AbstractListener[] buildListeners() {
    BarriersService barriersService = getService(BarriersService.class);
    return new AbstractListener[] {
        new BarrierSpawner(barriersService),
        new BarrierGuarder(barriersService),
        new PlayerBlocker(barriersService),
        new BarrierRefresher(this, barriersService)
    };
  }

  @Override
  protected void registerParsers(ParserManager parserManager) {
    parserManager.setDefaultParserForType(
        Barrier.class, new BarrierParser(getService(BarriersService.class), "barrier", "")
    );
  }

  @Override
  protected AbstractService[] buildServices() {
    BarriersService barriersService = new BarriersService();

    return new AbstractService[] {
        barriersService
    };
  }
}
