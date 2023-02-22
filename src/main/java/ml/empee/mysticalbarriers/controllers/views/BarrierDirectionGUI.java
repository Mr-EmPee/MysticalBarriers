package ml.empee.mysticalbarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.mysticalbarriers.MysticalBarriersPlugin;
import ml.empee.mysticalbarriers.controllers.BarrierController;
import ml.empee.mysticalbarriers.controllers.parsers.BarrierDirection;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.utils.reflection.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierDirectionGUI {

  private final MysticalBarriersPlugin plugin = JavaPlugin.getPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController = plugin.getIocContainer().getBean(BarrierController.class);
  private final RyseInventory inventory = RyseInventory.builder()
    .title("Choose a direction").rows(3)
    .disableUpdateTask()
    .provider(getProvider())
    .build(plugin);
  private final Barrier barrier;

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
    if (ServerVersion.isGreaterThan(1, 16, 2)) {
      material = Material.TNT_MINECART;
    } else {
      material = Material.valueOf("POWERED_MINECART");
    }

    return IntelligentItem.of(
      ItemBuilder.from(material).setName("&eNone").build(),
      e -> {
        barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.NONE);
        e.getWhoClicked().closeInventory();
      }
    );
  }

  private IntelligentItem all() {
    return IntelligentItem.of(
      ItemBuilder.from(Material.BOOK).setName("&eAll").build(),
      e -> {
        barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.ALL);
        e.getWhoClicked().closeInventory();
      }
    );
  }

  private IntelligentItem eastWest() {
    return IntelligentItem.of(
      ItemBuilder.from(Material.PAPER).setName("&eEast-West").build(),
      e -> {
        barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.EAST_WEST);
        e.getWhoClicked().closeInventory();
      }
    );
  }

  private IntelligentItem northSouth() {
    return IntelligentItem.of(
      ItemBuilder.from(Material.PAPER).setName("&eNorth-South").build(),
      e -> {
        barrierController.modifyBarrierDirection(e.getWhoClicked(), barrier, BarrierDirection.NORTH_SOUTH);
        e.getWhoClicked().closeInventory();
      }
    );
  }

}
