package ml.empee.mysticalbarriers.config;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.utils.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public class CommandsConfig implements Bean {

  private final PaperCommandManager<CommandSender> commandManager;
  private final AnnotationParser<CommandSender> commandParser;

  public CommandsConfig(JavaPlugin plugin) throws Exception {
    commandManager = new PaperCommandManager<>(
        plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity()
    );

    registerExceptionHandlers();

    commandParser = new AnnotationParser<>(
        commandManager, CommandSender.class, parameters -> SimpleCommandMeta.empty()
    );

    try {
      commandManager.registerBrigadier();
    } catch (BukkitCommandManager.BrigadierFailureException e) {
      Logger.warning("Command suggestion not supported! If you think this is an error make sure to use paper");
    }
  }

  private void registerExceptionHandlers() {
    commandManager.registerExceptionHandler(NoPermissionException.class, (sender, e) -> {
      Logger.translatedLog(sender, "cmd-no-permission");
    });

    commandManager.registerExceptionHandler(InvalidSyntaxException.class, (sender, e) -> {
      Logger.translatedLog(sender, "cmd-invalid-syntax");
    });

    commandManager.registerExceptionHandler(InvalidCommandSenderException.class, (sender, e) -> {
      Logger.translatedLog(sender, "cmd-invalid-sender");
    });

    commandManager.registerExceptionHandler(ArgumentParseException.class, (sender, e) -> {
      Logger.translatedLog(sender, "cmd-invalid-argument", e.getCause().getMessage());
    });

    commandManager.registerExceptionHandler(Exception.class, (sender, e) -> {
      Logger.translatedLog(sender, "cmd-exception");
    });
  }

  public <T> void register(T command) {
    commandParser.parse(command);
  }

}
