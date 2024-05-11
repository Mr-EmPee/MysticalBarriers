package core.controllers.guis;

import io.github.empee.easygui.guis.inventories.ChestGUI;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.easygui.model.inventories.Slot;
import io.github.empee.easygui.model.inventories.containers.PageContainer;
import io.github.empee.itembuilder.StackBuilder;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import core.controllers.guis.themes.global.GlobalTheme;
import core.model.Barrier;
import core.services.BarriersService;
import utils.TextUtils;

import java.util.List;

@LightWired
@RequiredArgsConstructor
public class BarrierListGUI extends PluginGUI {

  private final BarriersService barriersService;
  private final GlobalTheme globalTheme;

  public void open(Player player) {
    new Menu(player).open();
  }

  @RequiredArgsConstructor
  private class Menu {

    private final Player player;

    public void open() {
      var gui = ChestGUI.of(5);
      gui.title("Barriers");

      PageContainer barriersPage = PageContainer.of(gui, Slot.pane(gui, 1), barriers());
      gui.inserts(globalTheme.nextPage(barriersPage).slot(4, 8));
      gui.inserts(globalTheme.previousPage(barriersPage).slot(4, 0));

      gui.open(player);
    }

    private List<Item> barriers() {
      return barriersService.findAll().stream()
          .map(this::barrier)
          .toList();
    }

    private Item barrier(Barrier barrier) {
      var item = new StackBuilder(Material.BOOK)
          .withName(TextUtils.colorize("&e" + barrier.getId()))
          .toItemStack();

      return Item.of(item).clickHandler(
          e -> PluginGUI.get(BarrierEditGUI.class).open(player, barrier)
      );
    }

  }

}
