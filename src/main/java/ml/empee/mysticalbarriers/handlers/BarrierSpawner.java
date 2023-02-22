package ml.empee.mysticalbarriers.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.model.exceptions.MysticalBarrierException;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.model.adapters.MultiBlockPacket;
import ml.empee.mysticalbarriers.services.BarriersService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

@RequiredArgsConstructor
public class BarrierSpawner implements RegisteredListener, Bean {

  private final BarriersService barriersService;

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void sendBarriersBlocksOnPlayerMove(PlayerMoveEvent event) {
    if (!LocationUtils.hasChangedBlock(event.getFrom(), event.getTo())) {
      return;
    }

    sendBarriersBlocks(event.getTo(), event.getFrom(), event.getPlayer());
  }

  public void sendBarriersBlocks(Location toLoc, Location fromLoc, Player player) {
    for (Barrier barrier : barriersService.findAllBarriers()) {
      boolean isNearBarrier = barrier.isWithinRange(toLoc, null);
      boolean wasNearBarrier = barrier.isWithinRange(fromLoc, null);
      if (barrier.isHiddenFor(player) || (!isNearBarrier && !wasNearBarrier)) {
        continue;
      }

      sendBarrierBlocks(player, barrier, fromLoc, toLoc);
    }
  }

  public void sendBarrierBlocks(Player player, Barrier barrier, Location fromLoc, Location toLoc) {
    MultiBlockPacket packet = new MultiBlockPacket(toLoc, false);

    List<Location> visibleBarrierBlocks = new ArrayList<>();
    barrier.forEachVisibleBarrierBlock(toLoc, visibleBarrierBlocks::add);

    barrier.forEachVisibleBarrierBlock(fromLoc, (loc) -> {
      if (!visibleBarrierBlocks.remove(loc)) {
        packet.addBlock(Material.AIR, loc);
      }
    });

    for (Location loc : visibleBarrierBlocks) {
      packet.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), loc);
    }

    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException("Error while sending the barriers packet", e);
    }
  }

}
