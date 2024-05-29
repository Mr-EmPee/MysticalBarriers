package core.controllers.guis;

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
          .withLore(TextUtils.centered(
              """
                  &dLeft-Click &7to update the
                  &7barrier structure blocks

                  &dRight-Click &7to switch to
                  &7single block barrier wall
                  
                  &4&l!&c Switching will reset the current mask
                  """
          )).toItemStack();

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
          .withLore(TextUtils.centered(
              """
                  &dLeft-Click &7to get a wand that is able
                  &7to change the barrier wall material

                  &dRight-Click &7(ADVANCED) to switch
                  &7to multiple blocks type barrier wall
                  
                  &4&l! &cBefore switching it needs a mask of bedrock
                  &cto be able to tell which block should be part of
                  &cthe barrier structure
                  """
          )).toItemStack();

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
