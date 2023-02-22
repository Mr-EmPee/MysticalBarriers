package ml.empee.mysticalbarriers.controllers.views;

import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.enums.DisabledInventoryClick;
import io.github.rysefoxx.inventory.plugin.enums.InventoryOpenerType;
import io.github.rysefoxx.inventory.plugin.other.EventCreator;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.mysticalbarriers.MysticalBarriersPlugin;
import ml.empee.mysticalbarriers.controllers.BarrierController;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.utils.helpers.Logger;
import ml.empee.mysticalbarriers.utils.reflection.ServerVersion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierMaterialGUI {

  private final MysticalBarriersPlugin plugin = JavaPlugin.getPlugin(MysticalBarriersPlugin.class);
  private final Logger logger = plugin.getIocContainer().getBean(Logger.class);
  private final BarrierController barrierController = plugin.getIocContainer().getBean(BarrierController.class);
  private final RyseInventory inventory = RyseInventory.builder()
    .title("Insert a block")
    .type(InventoryOpenerType.DISPENSER)
    .listener(onClose())
    .ignoreClickEvent(DisabledInventoryClick.BOTTOM)
    .ignoredSlots(4)
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
        contents.fillBorders(background());
        contents.set(4, new ItemStack(Material.AIR));
      }
    };
  }

  private EventCreator<InventoryCloseEvent> onClose() {
    return new EventCreator<>(InventoryCloseEvent.class, e -> {
      ItemStack barrierMaterial = e.getInventory().getItem(4);
      if (barrierMaterial != null) {
        if (!barrierMaterial.getType().isBlock()) {
          logger.error(e.getPlayer(), "The barrier material must be a block!");
          return;
        }

        barrierController.modifyBarrierMaterial(e.getPlayer(), barrier, barrierMaterial.getType());
      }
    });
  }

  private ItemStack background() {
    if (ServerVersion.isGreaterThan(1, 16)) {
      return ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build();
    }

    return ItemBuilder.from(
      new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 15)
    ).setLore(" ").build();
  }

}
