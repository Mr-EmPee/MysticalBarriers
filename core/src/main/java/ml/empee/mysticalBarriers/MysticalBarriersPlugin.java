package ml.empee.mysticalBarriers;

import ml.empee.commandsManager.command.Command;
import ml.empee.commandsManager.parsers.ParserManager;
import ml.empee.mysticalBarriers.controllers.commands.MysticalBarriersCommand;
import ml.empee.mysticalBarriers.controllers.commands.parsers.BarrierParser;
import ml.empee.mysticalBarriers.helpers.EmpeePlugin;
import ml.empee.mysticalBarriers.helpers.Metrics;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.AbstractService;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.services.listeners.AbstractListener;
import ml.empee.mysticalBarriers.services.listeners.BarrierBlocksSpawner;
import ml.empee.mysticalBarriers.services.listeners.BarrierGuard;
import ml.empee.mysticalBarriers.services.listeners.BarrierRefresher;
import ml.empee.notifier.SimpleNotifier;

public final class MysticalBarriersPlugin extends EmpeePlugin {

  @Override
  public void onEnable() {
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
        new BarrierRefresher(this, barriersService),
        new BarrierBlocksSpawner(barriersService),
        new BarrierGuard(this, barriersService)
    };
  }

  @Override
  protected void registerParsers(ParserManager parserManager) {
    parserManager.setDefaultParserForType(Barrier.class,
        new BarrierParser(getService(BarriersService.class), "barrier", ""));
  }

  @Override
  protected AbstractService[] buildServices() {
    return new AbstractService[] {
        new BarriersService()
    };
  }
}
