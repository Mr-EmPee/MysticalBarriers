package ml.empee.mysticalBarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.other.EventCreator;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.BarrierController;
import ml.empee.mysticalBarriers.model.Barrier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BarrierRangeGUI {

  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController;
  private final Barrier barrier;
  private final RyseInventory inventory = RyseInventory.builder()
      .title("Choose a range").rows(3)
      .disableUpdateTask()
      .listener(onClose())
      .provider(getProvider())
      .build(plugin);

  private int range;

  public BarrierRangeGUI(BarrierController barrierController, Barrier barrier) {
    this.barrierController = barrierController;
    this.barrier = barrier;
    this.range = barrier.getActivationRange();
  }

  public void open(Player player) {
    inventory.open(player);
  }

  private InventoryProvider getProvider() {
    return new InventoryProvider() {
      @Override
      public void init(Player player, InventoryContents contents) {
        contents.set(1, 2, remove(contents));
        contents.set(1, 4, range());
        contents.set(1, 6, add(contents));
      }
    };
  }

  private EventCreator<InventoryCloseEvent> onClose() {
    return new EventCreator<>(InventoryCloseEvent.class, e -> {
      if(range == barrier.getActivationRange()) {
        return;
      }

      barrierController.modifyBarrierRange(e.getPlayer(), barrier, range);
    });
  }

  private IntelligentItem add(InventoryContents contents) {
    return IntelligentItem.of(
        ItemBuilder.from(Material.STONE_BUTTON).name("&e+").build(),
        e -> {
          if(range < 16) {
            range++;
            contents.update(13, range());
          }
        }
    );
  }

  private ItemStack range() {
    return ItemBuilder.from(Material.BOW).name("&e" + range).build();
  }

  private IntelligentItem remove(InventoryContents contents) {
    return IntelligentItem.of(
        ItemBuilder.from(Material.STONE_BUTTON).name("&e-").build(),
        e -> {
          if(range > 1) {
            range--;
            contents.update(13, range());
          }
        }
    );
  }


}
