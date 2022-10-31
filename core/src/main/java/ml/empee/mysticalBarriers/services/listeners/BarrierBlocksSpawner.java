package ml.empee.mysticalBarriers.services.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import lombok.RequiredArgsConstructor;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.helpers.Triple;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.Logger;

@RequiredArgsConstructor
public class BarrierBlocksSpawner implements Listener {

  private final BarriersService barriersService;

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {

    Location toLoc = event.getTo();
    if(toLoc == null) {
      return;
    }

    toLoc = toLoc.getBlock().getLocation();
    Location fromLoc = event.getFrom().getBlock().getLocation();
    if(toLoc.equals(fromLoc)) {
      return;
    }

    spawnBarrier(toLoc, fromLoc, event.getPlayer());

  }

  private void spawnBarrier(Location toLoc, Location fromLoc, Player player) {
    MultiBlockPacket packet = new MultiBlockPacket(toLoc, false);
    for(Barrier barrier : barriersService.findAllBarriers()) {
      if(barrier.isHiddenFor(player) || !barrier.getWorld().equals(player.getWorld())) {
        continue;
      }

      List<Triple<Integer, Integer, Integer>> sentBlocks = new ArrayList<>();
      barrier.forEachVisibleBarrierBlock(toLoc, (x, y, z) -> {
        Logger.debug("Player %s is near barrier block at %s %s %s", player.getName(), x, y, z);
        packet.addBlock(barrier.getMaterial(), x, y, z);
        sentBlocks.add(new Triple<>(x, y, z));
      });

      barrier.forEachVisibleBarrierBlock(fromLoc, (x, y, z) -> {
        if(!sentBlocks.contains(new Triple<>(x, y, z))) {
          Logger.debug("Player %s was near barrier block at %s %s %s", player.getName(), x, y, z);
          packet.addBlock(Material.AIR, x, y, z);
        }
      });

    }

    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException("Error while sending the barriers packet", e);
    }
  }

}
