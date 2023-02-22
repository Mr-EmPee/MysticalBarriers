package ml.empee.mysticalbarriers.handlers;

import java.util.HashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.Permissions;
import ml.empee.mysticalbarriers.constants.PluginItems;
import ml.empee.mysticalbarriers.utils.helpers.Logger;
import ml.empee.mysticalbarriers.utils.reflection.ServerVersion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BarrierDefiner implements RegisteredListener, Bean {

  private final HashMap<Player, Location[]> selectedCorners = new HashMap<>();
  private final Logger logger;
  private final PluginItems pluginItems;

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }

    if (ServerVersion.isGreaterThan(1, 9) && event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    }

    if (event.getItem() == null) {
      return;
    }

    if (!pluginItems.getSelectionWand().isPluginItem(event.getItem())) {
      return;
    }

    event.setCancelled(true);
    Player player = event.getPlayer();
    if (!player.hasPermission(Permissions.ADMIN_PERMISSION)) {
      logger.error(player, "You don't have permission to define regions!");
      return;
    }

    if (event.getAction().name().contains("RIGHT")) {
      onValidClick(player, event.getClickedBlock().getLocation());
    }
  }

  public void onValidClick(Player player, Location location) {
    Location[] locations = selectedCorners.computeIfAbsent(player, p -> new Location[2]);

    if (locations[0] != null && locations[1] != null) {
      locations[0] = null;
      locations[1] = null;
    }

    if (locations[0] == null) {
      locations[0] = location;
      logger.info(
        player, "First corner set to &eX%s Y%s Z%s",
        location.getBlockX(), location.getBlockY(), location.getBlockZ()
      );
    } else {
      if (!Objects.equals(locations[0].getWorld(), location.getWorld())) {
        logger.error(player, "The two corners must be in the same dimension!");
        return;
      }

      locations[1] = location;
      logger.info(
        player, "Second corner set to &eX%s Y%s Z%s",
        location.getBlockX(), location.getBlockY(), location.getBlockZ()
      );
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    selectedCorners.remove(event.getPlayer());
  }

  @NotNull
  public Location[] getSelectedCorners(Player player) {
    return selectedCorners.getOrDefault(player, new Location[2]);
  }

  public void clearSelectedCorners(Player player) {
    selectedCorners.remove(player);
  }

}
