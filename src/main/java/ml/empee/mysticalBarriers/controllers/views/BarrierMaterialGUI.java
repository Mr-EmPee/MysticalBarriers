package ml.empee.mysticalBarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.enums.DisabledInventoryClick;
import io.github.rysefoxx.inventory.plugin.enums.InventoryOpenerType;
import io.github.rysefoxx.inventory.plugin.other.EventCreator;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import lombok.RequiredArgsConstructor;
import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.BarrierController;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.utils.MCLogger;
import ml.empee.mysticalBarriers.utils.reflection.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierMaterialGUI {

  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController;
  private final Barrier barrier;
  private final RyseInventory inventory = RyseInventory.builder()
      .title("Insert a block")
      .type(InventoryOpenerType.DISPENSER)
      .listener(onClose())
      .ignoreClickEvent(DisabledInventoryClick.BOTTOM)
      .ignoredSlots(4)
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
        contents.fillBorders(background());
        contents.set(4, new ItemStack(Material.AIR));
      }
    };
  }

  private EventCreator<InventoryCloseEvent> onClose() {
    return new EventCreator<>(InventoryCloseEvent.class, e -> {
      ItemStack barrierMaterial = e.getInventory().getItem(4);
      if(barrierMaterial != null) {
        if(!barrierMaterial.getType().isBlock()) {
          MCLogger.error(e.getPlayer(), "The barrier material must be a block!");
          return;
        }

        barrierController.modifyBarrierMaterial(e.getPlayer(), barrier, barrierMaterial.getType());
      }
    });
  }

  private ItemStack background() {
    if(ServerVersion.isGreaterThan(1, 16)) {
      return ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
    }

    return ItemBuilder.from(new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 15)).name(" ").build();
  }

}
