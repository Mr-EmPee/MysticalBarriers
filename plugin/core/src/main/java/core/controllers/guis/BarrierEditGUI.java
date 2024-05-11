package core.controllers.guis;

import io.github.empee.easygui.guis.inventories.ChestGUI;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.itembuilder.StackBuilder;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import core.controllers.guis.commons.IntPickerGUI;
import core.items.BarrierBlockSelectorWand;
import core.model.Barrier;
import core.services.BarriersService;
import utils.Messenger;
import utils.TextUtils;

import java.util.function.Consumer;

@LightWired
@RequiredArgsConstructor
public class BarrierEditGUI extends PluginGUI {

  private final BarriersService barriersService;
  private final BarrierBlockSelectorWand blockSelectorWand;

  public void open(Player player, Barrier barrier) {
    new Menu(player, barrier).open();
  }

  @RequiredArgsConstructor
  private class Menu {

    private final Player player;
    private final Barrier barrier;

    public void open() {
      var gui = ChestGUI.of(3);
      gui.title("Editing: " + barrier.getId());

      gui.inserts(changeMaterial().slot(1, 1));
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

        barrier.setActivationRange(value);
        barriersService.save(barrier);

        Messenger.log(player, "&aThe barrier activation range has been changed");
      };

      return Item.of(item).clickHandler(e ->
          IntPickerGUI.of(barrier.getActivationRange(), action).open(player)
      );
    }

    private Item changeMaterial() {
      var item = new StackBuilder(Material.GRASS_BLOCK)
          .withName(TextUtils.colorize("&eChange barrier material"))
          .withLore(TextUtils.colorize("&7Current: &e" + barrier.getMaterial().getMaterial()))
          .toItemStack();

      return Item.of(item).clickHandler(e -> {
        player.closeInventory();
        player.getInventory().addItem(blockSelectorWand.get(barrier));
        Messenger.log(player, "&aUse the given wand to select a block");
      });
    }

    private Item delete() {
      var item = new StackBuilder(Material.TNT_MINECART)
          .withName(TextUtils.colorize("&eDestroy the barrier"))
          .toItemStack();

      return Item.of(item).clickHandler(e -> {
        player.closeInventory();
        barriersService.delete(barrier);
        Messenger.log(player, "&aThe barrier has been deleted");
      });
    }

  }

}
