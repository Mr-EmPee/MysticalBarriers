package core.controllers.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.empee.colonel.BrigadierCommand;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import core.MysticalBarriers;
import core.controllers.guis.BarrierListGUI;
import core.controllers.guis.PluginGUI;
import core.model.Barrier;
import core.registries.Permissions;
import core.items.RegionSelectorWand;
import core.services.BarriersService;
import utils.Messenger;

@LightWired
@RequiredArgsConstructor
public class BarriersCommand extends BrigadierCommand<CommandSender> {

  private final MysticalBarriers plugin;
  private final RegionSelectorWand selectionWand;
  private final BarriersService barriersService;

  @Override
  public LiteralArgumentBuilder<CommandSender> get() {
    return literal(MysticalBarriers.COMMAND)
        .requires(s -> s.hasPermission(Permissions.ADMIN))
        .then(wand())
        .then(create())
        .then(modify())
        .then(reload());
  }

  public ArgumentBuilder<CommandSender, ?> wand() {
    return node(literal("wand"))
        .executes(c -> {
          var player = player(c);
          player.getInventory().addItem(selectionWand.get());
          Messenger.log(player, "&aSelection wand given");
        }).build();
  }

  public ArgumentBuilder<CommandSender, ?> modify() {
    return node(literal("modify"))
        .executes(c -> PluginGUI.get(BarrierListGUI.class).open(player(c)))
        .build();
  }

  public ArgumentBuilder<CommandSender, ?> create() {
    return node(literal("create"), arg("id", StringArgumentType.string()))
        .executes(c -> {
          var player = player(c);
          var id = c.getArgument("id", String.class);
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
        }).build();
  }

  public ArgumentBuilder<CommandSender, ?> reload() {
    return node(literal("reload"))
        .executes(c -> plugin.reload())
        .build();
  }

}
