package ml.empee.mysticalBarriers.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

@RequiredArgsConstructor
public class BarrierSpawner extends AbstractListener {
  private final BarriersService barriersService;

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void sendBarriersBlocksOnPlayerMove(PlayerMoveEvent event) {
    if(!LocationUtils.hasChangedBlock(event.getFrom(), event.getTo())) {
      return;
    }

    sendBarriersBlocks(event.getTo(), event.getFrom(), event.getPlayer());
  }

  private void sendBarriersBlocks(Location toLoc, Location fromLoc, Player player) {
    for (Barrier barrier : barriersService.findAllBarriers()) {
      if (barrier.isHiddenFor(player) || (!barrier.isWithinRange(toLoc, null) && !barrier.isWithinRange(fromLoc, null))) {
        continue;
      }

      sendBarrierBlocks(player, barrier, fromLoc, toLoc);
    }
  }

  private void sendBarrierBlocks(Player player, Barrier barrier, Location fromLoc, Location toLoc) {
    MultiBlockPacket packet = new MultiBlockPacket(toLoc, false);

    List<Location> visibleBarrierBlocks = new ArrayList<>();
    barrier.forEachVisibleBarrierBlock(toLoc, visibleBarrierBlocks::add);

    barrier.forEachVisibleBarrierBlock(fromLoc, (loc) -> {
      if(!visibleBarrierBlocks.remove(loc)) {
        packet.addBlock(Material.AIR, loc);
      }
    });

    for(Location loc : visibleBarrierBlocks) {
      packet.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), loc);
    }

    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException("Error while sending the barriers packet", e);
    }
  }

}
