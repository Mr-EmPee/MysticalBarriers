package ml.empee.mysticalbarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.mysticalbarriers.MysticalBarriersPlugin;
import ml.empee.mysticalbarriers.controllers.BarrierController;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.utils.reflection.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierEditingGUI {

  private final MysticalBarriersPlugin plugin = JavaPlugin.getPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController = plugin.getIocContainer().getBean(BarrierController.class);
  private final RyseInventory inventory = RyseInventory.builder()
    .title("Barrier editing").rows(4)
    .disableUpdateTask()
    .provider(getProvider())
    .build(plugin);
  private final Barrier barrier;

  private InventoryProvider getProvider() {
    return new InventoryProvider() {
      @Override
      public void init(Player player, InventoryContents contents) {
        contents.set(1, 1, renameBarrier());
        contents.set(2, 1, deleteBarrier());
        contents.set(1, 3, redefineBarrierCorners());
        contents.set(2, 4, modifyBarrierDirection());
        contents.set(1, 7, modifyBarrierMaterial());
        contents.set(2, 6, modifyBarrierRange());
      }
    };
  }

  private IntelligentItem renameBarrier() {
    Material material;
    if (ServerVersion.isGreaterThan(1, 16, 2)) {
      material = Material.WRITABLE_BOOK;
    } else {
      material = Material.valueOf("BOOK_AND_QUILL");
    }

    return IntelligentItem.of(
      ItemBuilder.from(material).setName("&eRename").build(),
      e -> new BarrierRenamingGUI(barrier).open((Player) e.getWhoClicked())
    );
  }

  private IntelligentItem deleteBarrier() {
    Material material;
    if (ServerVersion.isGreaterThan(1, 16, 2)) {
      material = Material.TNT_MINECART;
    } else {
      material = Material.valueOf("POWERED_MINECART");
    }

    return IntelligentItem.of(
      ItemBuilder.from(material).setName("&eDelete").build(),
      e -> {
        barrierController.removeBarrier(e.getWhoClicked(), barrier);
        e.getWhoClicked().closeInventory();
      }
    );
  }

  private IntelligentItem redefineBarrierCorners() {
    return IntelligentItem.of(
      ItemBuilder.from(Material.PAPER).setName("&eRedefine borders").build(),
      e -> {
        barrierController.modifyBarrierCorners((Player) e.getWhoClicked(), barrier);
        e.getWhoClicked().closeInventory();
      }
    );
  }

  private IntelligentItem modifyBarrierRange() {
    return IntelligentItem.of(
      ItemBuilder.from(Material.BOW).setName("&eModify range").build(),
      e -> new BarrierRangeGUI(barrier).open((Player) e.getWhoClicked())
    );
  }

  private IntelligentItem modifyBarrierMaterial() {
    return IntelligentItem.of(
      ItemBuilder.from(Material.GLASS).setName("&eModify material").build(),
      e -> new BarrierMaterialGUI(barrier).open((Player) e.getWhoClicked())
    );
  }

  private IntelligentItem modifyBarrierDirection() {
    return IntelligentItem.of(
      ItemBuilder.from(Material.BIRCH_FENCE).setName("&eModify direction").build(),
      e -> new BarrierDirectionGUI(barrier).open((Player) e.getWhoClicked())
    );
  }

  public void open(Player player) {
    inventory.open(player);
  }

}
