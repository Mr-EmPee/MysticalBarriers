package core.controllers.commands;

import com.github.empee.commands.CommandNode;
import com.github.empee.commands.arguments.StringArgument;
import core.MysticalBarriers;
import core.controllers.commands.arguments.BarrierArgument;
import core.controllers.guis.BarrierEditGUI;
import core.controllers.guis.BarrierListGUI;
import core.controllers.guis.PluginGUI;
import core.items.RegionSelectorWand;
import core.registries.Permissions;
import core.services.BarriersService;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import utils.Messenger;

@LightWired
@RequiredArgsConstructor
public class BarriersCommand implements ICommand {

  private final MysticalBarriers plugin;
  private final RegionSelectorWand selectionWand;
  private final BarriersService barriersService;

  public CommandNode<CommandSender> get() {
    return CommandNode.of("mysticalbarriers", CommandSender.class)
        .withPermission(s -> s.hasPermission(Permissions.ADMIN))
        .withAliases("mb")
        .withChild(wand())
        .withChild(create())
        .withChild(modify())
        .withChild(list())
        .withChild(reload());
  }

  public CommandNode<Player> wand() {
    return CommandNode.of("wand", Player.class)
        .withExecutor(c -> {
          Player player = c.getSource();

          player.getInventory().addItem(selectionWand.get());
          Messenger.log(player, "&aSelection wand given");
        });
  }

  public CommandNode<Player> modify() {
    return CommandNode.of("edit", Player.class)
        .withArgs(BarrierArgument.of("barrier"))
        .withExecutor(c -> {
          PluginGUI.get(BarrierEditGUI.class).open(c.getSource(), c.get("barrier"));
        });
  }

  public CommandNode<Player> list() {
    return CommandNode.of("list", Player.class)
        .withExecutor(c -> {
          PluginGUI.get(BarrierListGUI.class).open(c.getSource());
        });
  }

  public CommandNode<Player> create() {
    return CommandNode.of("create", Player.class)
        .withArgs(StringArgument.of("id"))
        .withExecutor(c -> {
          Player player = c.getSource();
          String id = c.get("id");

          if (barriersService.findById(id).isPresent()) {
            Messenger.log(player, "&cA barrier with that id already exists");
            return;
          }

          var selection = selectionWand.getSelection(player.getUniqueId()).orElse(null);
          if (selection == null || !selection.isValid()) {
            Messenger.log(player, "&cYou haven't made a valid selection");
            return;
          }

          barriersService.createBarrier(id, selection);
          selectionWand.invalidate(player.getUniqueId());

          Messenger.log(player, "&aBarrier created");
        });
  }

  public CommandNode<CommandSender> reload() {
    return CommandNode.of("reload", CommandSender.class)
        .withExecutor(c -> {
          plugin.reload();
          
          Messenger.log(c.getSource(), "&aThe plugin has been reloaded");
        });
  }

}
