package ml.empee.mysticalbarriers.handlers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.constants.ItemRegistry;
import ml.empee.mysticalbarriers.constants.Permissions;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.helpers.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

/**
 * Control the selection that a player makes
 */

public class BarrierSelectionHandler implements Bean, RegisteredListener {

  private final Cache<Player, CuboidRegion> selectionCache = CacheBuilder.newBuilder()
      .expireAfterAccess(5, TimeUnit.MINUTES)
      .build();

  /**
   * Select a block by clicking on it with the barrier wand
   */
  @EventHandler
  private void onPlayerClickBlock(PlayerInteractEvent event) {
    if (event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    } else if (!event.hasBlock() || !event.hasItem() || event.getClickedBlock() == null) {
      return;
    } else if (!event.getPlayer().hasPermission(Permissions.ADMIN)) {
      return;
    }

    ItemStack item = event.getItem();
    if (!ItemRegistry.SELECTION_WAND.isPluginItem(item)) {
      return;
    }

    event.setCancelled(true);
    selectCorner(event.getPlayer(), event.getAction(), event.getClickedBlock().getLocation());
  }

  /**
   * Set a corner of the player selection
   */
  public void selectCorner(Player player, Action action, Location location) {
    CuboidRegion selection = getSelection(player);
    if (action.isRightClick()) {
      selection.setFirstCorner(location);
      Logger.log(player, "&7Selected first corner");
    } else {
      selection.setSecondCorner(location);
      Logger.log(player, "&7Selected second corner");
    }
  }

  @SneakyThrows
  public CuboidRegion getSelection(Player player) {
    return selectionCache.get(player, CuboidRegion::empty);
  }

}
