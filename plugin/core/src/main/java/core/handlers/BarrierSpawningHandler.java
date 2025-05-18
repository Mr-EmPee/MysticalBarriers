package core.handlers;

import core.model.Barrier;
import core.packets.MultiBlockPacket;
import core.services.BarriersService;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import utils.LocationUtils;
import utils.regions.CubicRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LightWired
@RequiredArgsConstructor
public class BarrierSpawningHandler implements Listener {

  private final BarriersService barriersService;
  private final Map<UUID, Map<Barrier, CubicRegion>> spawnedBarriers = new HashMap<>();

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if(event.getFrom().toVector().equals(event.getTo().toVector())) {
      return;
    }

    refreshBarriers(LocationUtils.toBlockLocation(event.getTo()), event.getPlayer());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    refreshBarriers(player.getLocation(), player);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    spawnedBarriers.remove(event.getPlayer().getUniqueId());
  }

  //TODO Async
  //TODO Despawn only out of range and spawn only new blocks
  private void refreshBarriers(Location loc, Player player) {
    despawnBarriersNear(loc, player);
    spawnBarriersNear(loc, player);
  }

  private void spawnBarriersNear(Location loc, Player player) {
    var newVisibleRegions = findRegionsWithinVisibleRange(loc);
    newVisibleRegions.forEach((barrier, region) -> {
      if (barriersService.isHidden(barrier, player)) {
        return;
      }

      var packet = new MultiBlockPacket(loc, true);
      region.forEach(l -> {
        var serverBlock = l.getBlock();
        if (serverBlock.isEmpty()) {
          var barrierBlock = barrier.getBlockDataAt(l);
          if (barrierBlock == null) {
            return;
          }

          packet.addBlock(barrierBlock, l);
        }
      });

      packet.send(player);
    });

    spawnedBarriers.put(player.getUniqueId(), newVisibleRegions);
  }

  private void despawnBarriersNear(Location loc, Player player) {
    var lastVisibleRegions = spawnedBarriers.getOrDefault(player.getUniqueId(), new HashMap<>());

    lastVisibleRegions.forEach((barrier, region) -> {
      var packet = new MultiBlockPacket(loc, true);
      region.forEach(l -> {
        if (l.getBlock().isEmpty()) {
          packet.addBlock(Material.AIR, l);
        }
      });
      packet.send(player);
    });
  }

  private Map<Barrier, CubicRegion> findRegionsWithinVisibleRange(Location newLoc) {
    var visibleRegions = new HashMap<Barrier, CubicRegion>();

    for (Barrier barrier : barriersService.findAll()) {
      var visibleRegion = barrier.findIntersection(CubicRegion.of(newLoc, barrier.getActivationRange()));
      if (visibleRegion != null) {
        visibleRegions.put(barrier, visibleRegion);
      }
    }

    return visibleRegions;
  }

}
