package core.items;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import core.registries.Permissions;
import io.github.empee.itembuilder.StackBuilder;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import utils.Messenger;
import utils.TextUtils;
import utils.regions.CubicRegion;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@LightWired
public class RegionSelectorWand extends PluginItem implements Listener {

  private final Cache<UUID, CubicRegion> selections = CacheBuilder.newBuilder()
      .expireAfterWrite(5, TimeUnit.MINUTES)
      .build();

  public RegionSelectorWand(JavaPlugin plugin) {
    super(new StackBuilder(Material.STICK), new NamespacedKey(plugin, "barrier_wand"));
  }

  protected void setup() {
    template.withName(TextUtils.colorize("&eBarrier Wand"));
  }

  @EventHandler
  public void onClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }

    if (!event.getAction().name().contains("RIGHT")) {
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
    if (!player.hasPermission(Permissions.ADMIN)) {
      player.getInventory().remove(item);
      Messenger.log(player, "&cYou don't have permission to define regions!");
      return;
    }

    var location = event.getClickedBlock().getLocation().toBlockLocation();

    select(player, location);
    Messenger.log(player, "&aSelected point at &e{} {} {}", location.getX(), location.getY(), location.getZ());
  }

  @SneakyThrows
  private int select(Player player, Location location) {
    var selection = selections.get(player.getUniqueId(), CubicRegion::new);
    return selection.add(location);
  }

  public Optional<CubicRegion> getSelection(UUID target) {
    return Optional.ofNullable(selections.getIfPresent(target));
  }

  public void invalidate(UUID target) {
    selections.invalidate(target);
  }

  public ItemStack get() {
    return template.toItemStack();
  }

}
