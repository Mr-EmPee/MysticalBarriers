package ml.empee.mysticalbarriers.controllers.views;

import io.github.rysefoxx.inventory.anvilgui.AnvilGUI;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.enums.InventoryOpenerType;
import io.github.rysefoxx.inventory.plugin.pagination.RyseAnvil;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.mysticalbarriers.MysticalBarriersPlugin;
import ml.empee.mysticalbarriers.controllers.BarrierController;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierRenamingGUI {

  private final MysticalBarriersPlugin plugin = JavaPlugin.getPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController = plugin.getIocContainer().getBean(BarrierController.class);
  private final RyseInventory inventory = RyseInventory.builder()
    .title("Choose a new name")
    .type(InventoryOpenerType.ANVIL)
    .provider(getProvider())
    .disableUpdateTask()
    .build(plugin);
  private final Barrier barrier;

  public void open(Player player) {
    inventory.open(player);
  }

  private InventoryProvider getProvider() {
    return new InventoryProvider() {
      @Override
      public void anvil(Player player, RyseAnvil anvil) {
        anvil.itemLeft(left());
        anvil.onComplete(e -> {
          onComplete(e.getPlayer(), e.getText());
          return Collections.singletonList(AnvilGUI.ResponseAction.close());
        });
      }
    };
  }

  private void onComplete(CommandSender sender, String name) {
    barrierController.modifyBarrierName(sender, barrier, name);
  }

  private ItemStack left() {
    return ItemBuilder.from(Material.NAME_TAG).setName(barrier.getId()).build();
  }

}
