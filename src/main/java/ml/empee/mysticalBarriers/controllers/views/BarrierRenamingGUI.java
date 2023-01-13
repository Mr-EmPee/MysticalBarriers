package ml.empee.mysticalBarriers.controllers.views;

import io.github.rysefoxx.inventory.anvilgui.AnvilGUI;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.enums.InventoryOpenerType;
import io.github.rysefoxx.inventory.plugin.pagination.RyseAnvil;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import ml.empee.itemBuilder.ItemBuilder;
import ml.empee.mysticalBarriers.MysticalBarriersPlugin;
import ml.empee.mysticalBarriers.controllers.BarrierController;
import ml.empee.mysticalBarriers.model.Barrier;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class BarrierRenamingGUI {
  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(MysticalBarriersPlugin.class);
  private final BarrierController barrierController;
  private final Barrier barrier;
  private final RyseInventory inventory = RyseInventory.builder()
      .title("Choose a new name")
      .type(InventoryOpenerType.ANVIL)
      .provider(getProvider())
      .disableUpdateTask()
      .build(plugin);

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
    return ItemBuilder.from(Material.NAME_TAG).name(barrier.getId()).build();
  }

}
