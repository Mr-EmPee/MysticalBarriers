package ml.empee.mysticalBarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import lombok.RequiredArgsConstructor;
import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.BarrierController;
import ml.empee.mysticalBarriers.controllers.parsers.BarrierDirection;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.utils.nms.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierDirectionGUI {

  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController;
  private final Barrier barrier;
  private final RyseInventory inventory = RyseInventory.builder()
      .title("Choose a direction").rows(3)
      .disableUpdateTask()
      .provider(getProvider())
      .build(plugin);

  public void open(Player player) {
    inventory.open(player);
  }

  private InventoryProvider getProvider() {
    return new InventoryProvider() {
      @Override
      public void init(Player player, InventoryContents contents) {
        contents.set(1, 1, none());
        contents.set(1, 4, eastWest());
        contents.set(1, 5, all());
        contents.set(1, 6, northSouth());
      }
    };
  }

  private IntelligentItem none() {
    Material material;
    if(ServerVersion.isGreaterThan(1, 16, 2)) {
      material = Material.TNT_MINECART;
    } else {
      material = Material.valueOf("POWERED_MINECART");
    }

    return IntelligentItem.of(
        ItemBuilder.from(material).name("&eNone").build(),
        e -> {
          barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.NONE);
          e.getWhoClicked().closeInventory();
        }
    );
  }

  private IntelligentItem all() {
    return IntelligentItem.of(
        ItemBuilder.from(Material.BOOK).name("&eAll").build(),
        e -> {
          barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.ALL);
          e.getWhoClicked().closeInventory();
        }
    );
  }

  private IntelligentItem eastWest() {
    return IntelligentItem.of(
        ItemBuilder.from(Material.PAPER).name("&eEast-West").build(),
        e -> {
          barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.EAST_WEST);
          e.getWhoClicked().closeInventory();
        }
    );
  }

  private IntelligentItem northSouth() {
    return IntelligentItem.of(
        ItemBuilder.from(Material.PAPER).name("&eNorth-South").build(),
        e -> {
          barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.NORTH_SOUTH);
          e.getWhoClicked().closeInventory();
        }
    );
  }

}
