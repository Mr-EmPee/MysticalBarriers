package ml.empee.mysticalBarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.Pagination;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import io.github.rysefoxx.inventory.plugin.pagination.SlotIterator;
import lombok.RequiredArgsConstructor;
import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.BarrierController;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.reflection.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarriersListGUI {

  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(MysticalBarriersPlugin.class);
  private final BarriersService barriersService;
  private final BarrierController barrierController;
  private final RyseInventory inventory = RyseInventory.builder()
      .title("Barriers list").rows(6)
      .disableUpdateTask()
      .provider(getProvider())
      .build(plugin);

  private InventoryProvider getProvider() {
    return new InventoryProvider() {
      @Override
      public void init(Player player, InventoryContents contents) {
        contents.fillBorders(background());

        Pagination pagination = contents.pagination();
        pagination.iterator(
            SlotIterator.builder()
                .startPosition(1, 1)
                .endPosition(4, 7)
                .build()
        );

        populateBarriers(pagination);
      }
    };
  }

  private void populateBarriers(Pagination pagination) {
    for (Barrier barrier : barriersService.findAllBarriers()) {
      pagination.addItem(IntelligentItem.of(
          barrierItem(barrier),
          e -> new BarrierEditingGUI(barrierController, barrier).open((Player) e.getWhoClicked())
      ));
    }
  }

  private ItemStack barrierItem(Barrier barrier) {
    return ItemBuilder.from(Material.BOOK)
        .name("&e" + barrier.getId())
        .lore(
            "",
            " &7Material&8: &d" + barrier.getMaterial(),
            " &7Activation range&8: &d" + barrier.getActivationRange(),
            " &7World: &d" + barrier.getWorld().getName(),
            " &7First corner&8: &d"
                + barrier.getFirstCorner().getBlockX() + "X "
                + barrier.getFirstCorner().getBlockY() + "Y "
                + barrier.getFirstCorner().getBlockZ() + "Z",
            " &7Second corner&8: &d"
                + barrier.getSecondCorner().getBlockX() + "X "
                + barrier.getSecondCorner().getBlockY() + "Y "
                + barrier.getSecondCorner().getBlockZ() + "Z",
            ""
        ).build();
  }

  private ItemStack background() {
    if(ServerVersion.isGreaterThan(1, 16)) {
      return ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
    }

    return ItemBuilder.from(new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 15)).name(" ").build();
  }

  public void open(Player player) {
    inventory.open(player);
  }

}
