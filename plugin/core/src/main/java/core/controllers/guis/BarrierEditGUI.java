package core.controllers.guis;

import core.configs.client.resources.MessagesConfig;
import core.controllers.guis.commons.IntPickerGUI;
import core.controllers.guis.themes.global.GlobalTheme;
import core.items.BarrierBlockSelectorWand;
import core.model.Barrier;
import core.services.BarriersService;
import io.github.empee.easygui.guis.inventories.ChestGUI;
import io.github.empee.easygui.model.inventories.Item;
import io.github.empee.itembuilder.StackBuilder;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import utils.Messenger;
import utils.TextUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@LightWired
@RequiredArgsConstructor
public class BarrierEditGUI extends PluginGUI {

  private final BarriersService barriersService;
  private final BarrierBlockSelectorWand barrierBlockSelectorWand;
  private final GlobalTheme globalTheme;
  private final MessagesConfig messagesConfig;

  public void open(Player player, Barrier barrier) {
    new Menu(player, barrier).open();
  }

  @AllArgsConstructor
  private class Menu {

    private final ChestGUI gui = ChestGUI.of(3);
    private final Player player;
    private Barrier barrier;

    public void open() {
      gui.title("Editing: " + barrier.getId());

      gui.inserts(globalTheme.previousGUI(e -> get(BarrierListGUI.class).open(player)).slot(2, 0));

      gui.inserts(updateMaterial().slot(1, 1));
      gui.inserts(updateStructure().slot(1, 1));

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

        barriersService.updateBarrierRange(barrier, value);

        Messenger.log(player, "&aThe barrier activation range has been changed");
      };

      return Item.of(item).clickHandler(e ->
          IntPickerGUI.builder()
              .value(new AtomicInteger(barrier.getActivationRange()))
              .back(globalTheme.previousGUI(g -> gui.open(player)).slot(2, 0))
              .action(action)
              .build().open(player)
      );
    }

    private Item updateStructure() {
      var item = new StackBuilder(Material.SCAFFOLDING)
          .withName(TextUtils.colorize("&eUpdate structure"))
          .withLore(TextUtils.formatted(messagesConfig.get("barriers.editor.update-structure.description")))
          .toItemStack();

      return Item.of(item)
          .priority(() -> barrier.getStructure() == null ? -1 : 1)
          .clickHandler(e -> {
            if (e.isRightClick()) {
              barriersService.removeBarrierStructure(barrier);
              gui.update();

              Messenger.log(player, "&aSwitching the barrier to single block barrier wall");
              return;
            }

            player.closeInventory();
            barriersService.updateBarrierStructure(barrier);

            Messenger.log(player, "&aThe barrier wall structure has been updated");
          });
    }

    private Item updateMaterial() {
      Supplier<ItemStack> item = () -> new StackBuilder(barrier.getFillBlock().getMaterial())
          .withName(TextUtils.colorize("&eChange Material"))
          .withLore(TextUtils.formatted(messagesConfig.get("barriers.editor.update-material.description")))
          .toItemStack();

      return Item.of(item)
          .priority(() -> barrier.getStructure() != null ? -1 : 1)
          .clickHandler(e -> {
            if (e.isRightClick()) {
              barriersService.updateBarrierStructureMask(barrier);
              gui.update();

              Messenger.log(player, "&aSwitching the barrier to multi block barrier wall");
              return;
            }

            player.closeInventory();
            player.getInventory().addItem(barrierBlockSelectorWand.get(barrier));
            Messenger.log(player, "&aUse the given wand to select block type");
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
