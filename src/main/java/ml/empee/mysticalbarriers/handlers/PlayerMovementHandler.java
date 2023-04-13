package ml.empee.mysticalbarriers.handlers;

import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.config.BarriersConfig;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

/**
 * Prevent a player from bypassing a barrier
 */

@RequiredArgsConstructor
public class PlayerMovementHandler implements Bean, RegisteredListener {

  private final BarrierService barrierService;
  private final BarriersConfig config;

  /**
   * Prevents players from going into a visible barrier
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPlayerMove(PlayerMoveEvent event) {
    //TODO: PlayerMoveEvent not called on player pixel-moving, listen for the packet instead
    if (LocationUtils.isSameBlock(event.getFrom(), event.getTo())) {
      return;
    }

    Player player = event.getPlayer();
    Barrier barrier = barrierService.findBarrierByBlock(event.getTo().getBlock()).orElse(null);
    if (barrier == null || !barrier.isVisibleFor(player)) {
      return;
    }

    event.setCancelled(true);
    if (player.getVehicle() != null) {
      player.getVehicle().eject();
    }
  }

  /**
   * Prevents players from teleporting into a visible barrier
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    Barrier barrier = barrierService.findBarrierByBlock(event.getTo().getBlock()).orElse(null);
    if (barrier == null || !barrier.isVisibleFor(event.getPlayer())) {
      return;
    }

    event.setCancelled(true);
  }

  /**
   * <p><b>Must be enabled from config!</b></p>
   * Prevents player teleport using chorus when near a visible barrier
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPlayerTeleportUsingChorus(PlayerTeleportEvent event) {
    if (!config.shouldBlockChorusTp()) {
      return;
    } else if (!event.getCause().name().equals("CHORUS_FRUIT")) {
      return;
    }

    List<Barrier> barriers = barrierService.findBarrierNear(event.getFrom());
    if (barriers.stream().noneMatch(b -> b.isVisibleFor(event.getPlayer()))) {
      return;
    }

    event.setCancelled(true);
  }

}
