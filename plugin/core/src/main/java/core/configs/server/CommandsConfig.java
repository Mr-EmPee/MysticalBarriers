package core.configs.server;

import com.github.empee.commands.CommandContext;
import com.github.empee.commands.CommandManager;
import com.github.empee.commands.CommandNode;
import com.github.empee.commands.exceptions.ArgumentException;
import com.github.empee.commands.exceptions.CommandException;
import com.github.empee.commands.spigot.BukkitInjector;
import core.configs.client.resources.MessagesConfig;
import core.controllers.commands.ICommand;
import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import utils.Messenger;

import java.util.List;
import java.util.Map;

@LightWired
public class CommandsConfig {

  CommandManager<CommandSender> commandManager = new CommandManager<>(CommandSender.class);

  private final JavaPlugin plugin;
  private final MessagesConfig messagesConfig;
  private final List<ICommand> commands;

  public CommandsConfig(JavaPlugin plugin, MessagesConfig messagesConfig, List<ICommand> commands) {
    this.messagesConfig = messagesConfig;
    this.plugin = plugin;
    this.commands = commands;

    registerExceptionHandler();
    registerCommands();
  }

  private void registerCommands() {
    BukkitInjector injector = new BukkitInjector(plugin, commandManager);

    for (ICommand command : commands) {
      CommandNode<? extends CommandSender> cmd = command.get();

      commandManager.register(cmd);
      injector.inject(command.get());
    }
  }

  private void registerExceptionHandler() {
    commandManager.setExceptionHandler(this::handleCommandException);
  }

  public void handleCommandException(CommandContext<CommandSender> context, CommandException exception) {
    if (exception.getType() == CommandException.Type.ARG_PARSING_ERROR) {
      handleCommandArgumentException(context, (ArgumentException) exception);
      return;
    }

    if (exception.getType() == CommandException.Type.EXECUTION_ERROR) {
      Messenger.error("Error while executing command '/{}'", exception.getCause(), context.getReader().getText());
    }

    Messenger.log(context.getSource(), messagesConfig.get("commands.errors." + exception.getType()));
  }

  private void handleCommandArgumentException(CommandContext<CommandSender> context, ArgumentException exception) {
    String message;

    if (exception.getCauseID() == null) {
      message = messagesConfig.get("commands.errors.arg_parsing_error.unk");
      Messenger.error("Error while parsing argument '{}' of command '/{}'", exception.getCause(), exception.getInput(), context.getReader().getText());
    } else {
      var placeholders = Map.of("input", (Object) exception.getInput());
      message = messagesConfig.get("commands.errors.arg_parsing_error." + exception.getCauseID(), placeholders);
    }

    Messenger.log(context.getSource(), message);
  }

}
