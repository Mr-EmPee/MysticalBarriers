package core.controllers.guis;

import core.configs.client.resources.MessagesConfig;
import core.controllers.guis.themes.global.GlobalTheme;
import core.model.Barrier;
import core.services.BarriersService;
import io.github.empee.easygui.guis.inventories.ChestGUI;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.easygui.model.inventories.Slot;
import io.github.empee.easygui.model.inventories.containers.ScrollableContainer;
import io.github.empee.itembuilder.StackBuilder;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import utils.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@LightWired
@RequiredArgsConstructor
public class BarrierListGUI extends PluginGUI {

  private final BarriersService barriersService;
  private final GlobalTheme globalTheme;
  private final MessagesConfig messagesConfig;

  public void open(Player player) {
    new Menu(player).open();
  }

  @RequiredArgsConstructor
  private class Menu {

    private final Player player;

    public void open() {
      var gui = ChestGUI.of(5);
      gui.title("Barriers");

      ScrollableContainer barriersPage = ScrollableContainer.fromCollection(gui, Slot.pane(gui, 1), barriers());
      gui.inserts(globalTheme.nextPage(barriersPage).slot(4, 8));
      gui.inserts(globalTheme.previousPage(barriersPage).slot(4, 0));

      gui.open(player);
    }

    private List<Item> barriers() {
      return barriersService.findAll().stream()
          .map(this::barrier)
          .collect(Collectors.toList());
    }

    private Item barrier(Barrier barrier) {
      Map<String, Object> placeholders = Map.of(
          "type", barrier.getStructure() != null ? "Multi Block" : "Single Block",
          "range", barrier.getActivationRange(),
          "loc_w", barrier.getRegion().getWorld().getName(),
          "loc_x", barrier.getRegion().getFirst().getBlockX(),
          "loc_y", barrier.getRegion().getFirst().getBlockY(),
          "loc_z", barrier.getRegion().getFirst().getBlockZ()
      );

      var item = new StackBuilder(Material.BOOK)
          .withName(TextUtils.colorize("&6" + barrier.getId()))
          .withLore(TextUtils.formatted(messagesConfig.get("barriers.selector.entry.description", placeholders)))
          .toItemStack();

      return Item.of(item).clickHandler(
          e -> PluginGUI.get(BarrierEditGUI.class).open(player, barrier)
      );
    }

  }

}
