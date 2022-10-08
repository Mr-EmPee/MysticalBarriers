package ml.empee.mysticalBarriers;

import org.bukkit.event.Listener;

import ml.empee.commandsManager.command.Command;
import ml.empee.commandsManager.parsers.ParserManager;
import ml.empee.mysticalBarriers.controllers.commands.MysticalBarriersCommand;
import ml.empee.mysticalBarriers.controllers.commands.parsers.BarrierParser;
import ml.empee.mysticalBarriers.controllers.components.BarrierDefiner;
import ml.empee.mysticalBarriers.helpers.EmpeePlugin;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.services.components.BarrierBlocksSpawner;
import ml.empee.mysticalBarriers.services.components.BarrierGuard;
import ml.empee.mysticalBarriers.services.components.BarrierRefresher;
import ml.empee.notifier.SimpleNotifier;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public final class MysticalBarriersPlugin extends EmpeePlugin {

  @Override
  public void onEnable() {
    adventure = BukkitAudiences.create(this);
    scheduleSimpleNotifier();
    super.onEnable();
  }

  private void scheduleSimpleNotifier() {
    getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
      SimpleNotifier.checkNotifications("105671", this);
    }, 0, 20 * 60 * 60);
  }

  @Override
  protected Command[] buildCommands() {
    return new Command[] {
        new MysticalBarriersCommand(getService(BarriersService.class))
    };
  }

  @Override
  protected Listener[] buildListeners() {
    BarriersService barriersService = getService(BarriersService.class);
    return new Listener[] {
        new BarrierRefresher(this, barriersService),
        new BarrierDefiner(barriersService),
        new BarrierBlocksSpawner(barriersService),
        new BarrierGuard(this, barriersService)
    };
  }

  @Override
  protected void registerParsers(ParserManager parserManager) {
    parserManager.setDefaultParserForType(Barrier.class, new BarrierParser(getService(BarriersService.class), "barrier", ""));
  }

  @Override
  protected Object[] buildServices() {
    return new Object[] {
        new BarriersService()
    };
  }
}
