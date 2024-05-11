package plugin.configs.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.empee.colonel.BrigadierCommand;
import io.github.empee.colonel.BrigadierManager;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.configs.client.resources.MessagesConfig;
import plugin.exceptions.PluginException;
import utils.Messenger;

import java.util.List;

@LightWired
public class CommandsConfig extends BrigadierManager<CommandSender> {
  private final MessagesConfig messages;

  public CommandsConfig(
      JavaPlugin plugin, MessagesConfig messages,
      List<BrigadierCommand<CommandSender>> commands
  ) {
    super(plugin, new CommandDispatcher<>());

    this.messages = messages;
    commands.forEach(c -> {
      var registeredLabel = register(c.get());
      Messenger.log("The command '{}' has been registered", registeredLabel);
    });
  }

  @Override
  @SneakyThrows
  protected void handleException(CommandSender source, Exception e) {
    if (e instanceof PluginException pl) {
      Messenger.log(source, messages.get(pl.getId(), pl.getArguments()));
    } else if (e instanceof CommandSyntaxException) {
      Messenger.log(source, e.getMessage());
    } else {
      Messenger.log(source, messages.get("errors.internal_error"));
      Messenger.log("Error while executing command", e);
    }
  }

  @Override
  protected CommandSender getSource(CommandSender source) {
    return source;
  }
}
