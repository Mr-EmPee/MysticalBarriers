package ml.empee.mysticalbarriers.handlers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.constants.ItemRegistry;
import ml.empee.mysticalbarriers.constants.Permissions;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.ServerVersion;
import ml.empee.mysticalbarriers.utils.helpers.CuboidSelection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

/**
 * Control the selection that a player makes
 */

public class BarrierSelectionHandler implements Bean, RegisteredListener {

  private boolean isOffhandEnabled = true;
  private final Cache<Player, CuboidSelection> selectionCache = CacheBuilder.newBuilder()
      .expireAfterAccess(5, TimeUnit.MINUTES)
      .build();

  @Override
  public void onStart() {
    if (ServerVersion.isLowerThan(1, 9)) {
      isOffhandEnabled = false;
    }
  }

  /**
   * Select a block by clicking on it with the barrier wand
   */
  @EventHandler
  @SuppressWarnings("checkstyle:CyclomaticComplexity")
  private void onPlayerClickBlock(PlayerInteractEvent event) {
    if (event.getAction().isLeftClick() || (isOffhandEnabled && event.getHand() == EquipmentSlot.OFF_HAND)) {
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

    selectCorner(event.getPlayer(), event.getClickedBlock().getLocation());
  }

  /**
   * Add a corner to the player selection
   */
  public void selectCorner(Player player, Location location) {
    CuboidSelection selection = getSelection(player);
    if (selection.getStart() == null || selection.getEnd() != null) {
      selection.setStart(location);
      selection.setEnd(null);

      Logger.log(player, "&7Selected first corner");
    } else {
      selection.setEnd(location);

      Logger.log(player, "&7Selected second corner");
    }
  }

  @SneakyThrows
  public CuboidSelection getSelection(Player player) {
    return selectionCache.get(player, CuboidSelection::empty);
  }

}
