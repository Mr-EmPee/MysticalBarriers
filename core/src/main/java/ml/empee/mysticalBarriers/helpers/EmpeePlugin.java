package ml.empee.mysticalBarriers.helpers;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import ml.empee.commandsManager.CommandManager;
import ml.empee.commandsManager.command.Command;
import ml.empee.commandsManager.parsers.ParserManager;
import ml.empee.mysticalBarriers.services.AbstractService;
import ml.empee.mysticalBarriers.services.listeners.AbstractListener;
import ml.empee.mysticalBarriers.utils.Logger;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public abstract class EmpeePlugin extends JavaPlugin {

  private AbstractService[] services;
  private AbstractListener[] listeners;

  protected CommandManager commandManager;
  protected BukkitAudiences adventure;

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
    commandManager.unregisterCommands();

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
    commandManager = new CommandManager(this, adventure);
    Logger.info("Baking commands...");
    registerParsers(commandManager.getParserManager());
    Command[] commands = buildCommands();

    Logger.info("Started commands registration process");
    int i = 1;
    for (Command command : commands) {
      Logger.info(" - (" + i + "/" + commands.length + ") Registering " + command.getClass().getSimpleName());
      commandManager.registerCommand(command);
      i++;
    }

    Logger.info("All commands have been registered");
  }

  private void registerListeners() {
    Logger.info("Baking listeners...");
    listeners = buildListeners();

    Logger.info("Started listener registration process");
    int i = 1;
    for (Listener listener : listeners) {
      Logger.info(" - (" + i + "/" + listeners.length + ") Registering " + listener.getClass().getSimpleName());
      getServer().getPluginManager().registerEvents(listener, this);
      i++;
    }

    Logger.info("All listeners have been registered");
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
