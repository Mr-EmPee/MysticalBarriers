package core.controllers.guis;

import io.github.empee.easygui.guis.inventories.ChestGUI;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.itembuilder.StackBuilder;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import core.controllers.guis.commons.IntPickerGUI;
import core.model.Barrier;
import core.services.BarriersService;
import utils.Messenger;
import utils.TextUtils;

import java.util.function.Consumer;

@LightWired
@RequiredArgsConstructor
public class BarrierEditGUI extends PluginGUI {

  private final BarriersService barriersService;

  public void open(Player player, Barrier barrier) {
    new Menu(player, barrier).open();
  }

  @AllArgsConstructor
  private class Menu {

    private final Player player;
    private Barrier barrier;

    public void open() {
      var gui = ChestGUI.of(3);
      gui.title("Editing: " + barrier.getId());

      gui.inserts(updateWall().slot(1, 1));
      gui.inserts(changeActivationRange().slot(1, 4));
      gui.inserts(delete().slot(1, 7));

      gui.open(player);
    }

    private Item changeActivationRange() {
      var item = new StackBuilder(Material.TARGET)
          .withName(TextUtils.colorize("&eChange activation range"))
          .toItemStack();

      Consumer<Integer> action = value -> {
        if (value <= 0) {
          Messenger.log(player, "&cThe range value must be greater then 0");
          return;
        }

        barrier = barriersService.updateBarrierRange(barrier.getId(), value);

        Messenger.log(player, "&aThe barrier activation range has been changed");
      };

      return Item.of(item).clickHandler(e ->
          IntPickerGUI.of(barrier.getActivationRange(), action).open(player)
      );
    }

    private Item updateWall() {
      var item = new StackBuilder(Material.SCAFFOLDING)
          .withName(TextUtils.colorize("&eSave barrier wall"))
          .toItemStack();

      return Item.of(item).clickHandler(e -> {
        player.closeInventory();

        barrier = barriersService.updateBarrierWall(barrier.getId());

        Messenger.log(player, "&aThe barrier wall has been updated");
      });
    }

    private Item delete() {
      var item = new StackBuilder(Material.TNT_MINECART)
          .withName(TextUtils.colorize("&eDestroy the barrier"))
          .toItemStack();

      return Item.of(item).clickHandler(e -> {
        player.closeInventory();

        barriersService.delete(barrier.getId());
        barrier = null;

        Messenger.log(player, "&aThe barrier has been deleted");
      });
    }

  }

}
