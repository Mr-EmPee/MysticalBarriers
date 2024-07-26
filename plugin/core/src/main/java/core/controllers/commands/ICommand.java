package core.controllers.commands;

import com.github.empee.commands.CommandNode;
import org.bukkit.command.CommandSender;

public interface ICommand {

  CommandNode<? extends CommandSender> get();

}
