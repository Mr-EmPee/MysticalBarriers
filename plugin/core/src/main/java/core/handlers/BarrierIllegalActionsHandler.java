package core.handlers;

import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import core.configs.client.resources.PluginConfig;
import core.model.Barrier;
import core.services.BarriersService;
import utils.regions.CubicRegion;

import java.util.ArrayList;
import java.util.List;

@LightWired
@RequiredArgsConstructor
public class BarrierIllegalActionsHandler implements Listener {

  private final BarriersService barriersService;
  private final PluginConfig pluginConfig;

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelOnChorusTeleportationNearBarrier(PlayerTeleportEvent event) {
    if (!pluginConfig.blockChorusTp() || !event.getCause().name().equals("CHORUS_FRUIT")) {
      return;
    }

    var player = event.getPlayer();
    var barriers = findBarriersWithinRange(event.getFrom(), 8);
    if (!barriers.isEmpty() && barriers.stream().anyMatch(b -> !barriersService.isHidden(b, player))) {
      event.setCancelled(true);
    }
  }

  private List<Barrier> findBarriersWithinRange(Location center, int range) {
    var barriers = new ArrayList<Barrier>();

    for (Barrier barrier : barriersService.findAll()) {
      var visibleRegion = barrier.findVisibleRegion(CubicRegion.of(center, range));
      if (visibleRegion != null) {
        barriers.add(barrier);
      }
    }

    return barriers;
  }

}
