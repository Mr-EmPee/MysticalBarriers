package ml.empee.mysticalBarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import lombok.RequiredArgsConstructor;
import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.BarrierController;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.utils.reflection.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierEditingGUI {

  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController;
  private final Barrier barrier;
  private final RyseInventory inventory = RyseInventory.builder()
      .title("Barrier editing").rows(4)
      .disableUpdateTask()
      .provider(getProvider())
      .build(plugin);

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
    if(ServerVersion.isGreaterThan(1, 16, 2)) {
      material = Material.WRITABLE_BOOK;
    } else {
      material = Material.valueOf("BOOK_AND_QUILL");
    }

    return IntelligentItem.of(
        ItemBuilder.from(material).name("&eRename").build(),
        e -> {
          new BarrierRenamingGUI(barrierController, barrier).open((Player) e.getWhoClicked());
        }
    );
  }

  private IntelligentItem deleteBarrier() {
    Material material;
    if(ServerVersion.isGreaterThan(1, 16, 2)) {
      material = Material.TNT_MINECART;
    } else {
      material = Material.valueOf("POWERED_MINECART");
    }
    
    return IntelligentItem.of(
        ItemBuilder.from(material).name("&eDelete").build(),
        e -> {
          barrierController.removeBarrier(e.getWhoClicked(), barrier);
          e.getWhoClicked().closeInventory();
        }
    );
  }

  private IntelligentItem redefineBarrierCorners() {
    return IntelligentItem.of(
        ItemBuilder.from(Material.PAPER).name("&eRedefine borders").build(),
        e -> {
          barrierController.modifyBarrierCorners((Player) e.getWhoClicked(), barrier);
          e.getWhoClicked().closeInventory();
        }
    );
  }

  private IntelligentItem modifyBarrierRange() {
    return IntelligentItem.of(
        ItemBuilder.from(Material.BOW).name("&eModify range").build(),
        e -> {
          new BarrierRangeGUI(barrierController, barrier).open((Player) e.getWhoClicked());
        }
    );
  }

  private IntelligentItem modifyBarrierMaterial() {
    return IntelligentItem.of(
        ItemBuilder.from(Material.GLASS).name("&eModify material").build(),
        e -> {
          new BarrierMaterialGUI(barrierController, barrier).open((Player) e.getWhoClicked());
        }
    );
  }

  private IntelligentItem modifyBarrierDirection() {
    return IntelligentItem.of(
        ItemBuilder.from(Material.BIRCH_FENCE).name("&eModify direction").build(),
        e -> {
          new BarrierDirectionGUI(barrierController, barrier).open((Player) e.getWhoClicked());
        }
    );
  }

  public void open(Player player) {
    inventory.open(player);
  }
  
}
