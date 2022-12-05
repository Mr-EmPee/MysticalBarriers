package ml.empee.mysticalBarriers;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import ml.empee.commandsManager.CommandManager;
import ml.empee.commandsManager.command.Command;
import ml.empee.commandsManager.parsers.ParserManager;
import ml.empee.mysticalBarriers.listeners.AbstractListener;
import ml.empee.mysticalBarriers.services.AbstractService;
import ml.empee.mysticalBarriers.utils.MCLogger;

public abstract class AbstractPlugin extends JavaPlugin {

  private AbstractService[] services = new AbstractService[0];
  private AbstractListener[] listeners = new AbstractListener[0];

  protected CommandManager commandManager;

  @Override
  public void onEnable() {
    registerAll();
  }

  @Override
  public void onDisable() {
    unregisterAll();
  }

  public void reload() {
    unregisterAll();
    registerAll();
  }

  protected void unregisterAll() {
    if (commandManager != null) {
      commandManager.unregisterCommands();
    }

    for (AbstractListener listener : listeners) {
      HandlerList.unregisterAll(listener);
    }

    for (AbstractService service : services) {
      service.stop();
    }

    services = new AbstractService[0];
  }

  protected void registerAll() {
    services = buildServices();
    registerListeners();
    registerCommands();
  }

  private void registerCommands() {
    commandManager = new CommandManager(this);
    MCLogger.info("Baking commands...");
    registerParsers(commandManager.getParserManager());
    Command[] commands = buildCommands();

    MCLogger.info("Started commands registration process");
    int i = 1;
    for (Command command : commands) {
      MCLogger.info(" - (" + i + "/" + commands.length + ") Registering " + command.getClass().getSimpleName());
      commandManager.registerCommand(command);
      i++;
    }

    MCLogger.info("All commands have been registered");
  }

  private void registerListeners() {
    MCLogger.info("Baking listeners...");
    listeners = buildListeners();

    MCLogger.info("Started listener registration process");
    int i = 1;
    for (Listener listener : listeners) {
      MCLogger.info(" - (" + i + "/" + listeners.length + ") Registering " + listener.getClass().getSimpleName());
      getServer().getPluginManager().registerEvents(listener, this);
      i++;
    }

    MCLogger.info("All listeners have been registered");
  }

  protected void registerParsers(ParserManager parserManager) {

  }

  protected AbstractListener[] buildListeners() {
    return new AbstractListener[0];
  }

  protected Command[] buildCommands() {
    return new Command[0];
  }

  protected AbstractService[] buildServices() {
    return new AbstractService[0];
  }

  public <T> T getListener(Class<T> clazz) {
    for (Listener listener : listeners) {
      if (listener.getClass().equals(clazz)) {
        return clazz.cast(listener);
      }
    }
    return null;
  }

  public <T> T getService(Class<T> clazz) {
    for (Object service : services) {
      if (service.getClass().equals(clazz)) {
        return clazz.cast(service);
      }
    }
    return null;
  }

}
