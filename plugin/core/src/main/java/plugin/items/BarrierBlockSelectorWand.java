package plugin.items;

import io.github.empee.itembuilder.StackBuilder;
import io.github.empee.itembuilder.utils.ItemstackUtils;
import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.model.Barrier;
import plugin.registries.Permissions;
import plugin.services.BarriersService;
import utils.Messenger;
import utils.TextUtils;

import java.util.Optional;

@LightWired
public class BarrierBlockSelectorWand extends PluginItem implements Listener {

  private final NamespacedKey barrierKey;
  private final BarriersService barriersService;

  public BarrierBlockSelectorWand(JavaPlugin plugin, BarriersService barriersService) {
    super(new StackBuilder(Material.STICK), new NamespacedKey(plugin, "block_selector_wand"));

    this.barrierKey = new NamespacedKey(plugin, "barrier_id");
    this.barriersService = barriersService;
  }

  protected void setup() {
    template.withName(TextUtils.colorize("&eBlock Selector Wand"));
  }

  @EventHandler
  public void onClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }

    if (!event.getAction().isRightClick()) {
      return;
    }

    var item = event.getItem();
    if (item == null) {
      return;
    }

    if (!isItem(item)) {
      return;
    }

    event.setCancelled(true);
    Player player = event.getPlayer();

    player.getInventory().remove(item);

    if (!player.hasPermission(Permissions.ADMIN)) {
      Messenger.log(player, "&cYou don't have permission to change a barrier material!");
      return;
    }

    var block = event.getClickedBlock();
    var barrier = getLinkedBarrier(item).orElse(null);
    if (barrier == null) {
      Messenger.log(player, "&cThe linked barrier doesn't exists anymore!");
      return;
    }

    barrier.setMaterial(block.getBlockData());
    barriersService.save(barrier);

    Messenger.log(player, "&aThe barrier material has been updated!");
  }

  private Optional<Barrier> getLinkedBarrier(ItemStack item) {
    var pdc = item.getItemMeta().getPersistentDataContainer();
    var barrierId = pdc.get(barrierKey, PersistentDataType.STRING);
    return barriersService.findById(barrierId);
  }

  public ItemStack get(Barrier barrier) {
    var result = template.toItemStack();

    ItemstackUtils.addLore(result, TextUtils.colorize("&7Linked to: &6" + barrier.getId()));
    ItemstackUtils.addPersistentTag(result, barrierKey, PersistentDataType.STRING, barrier.getId());

    return result;
  }


}
