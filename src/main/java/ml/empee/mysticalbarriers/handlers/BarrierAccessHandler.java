package ml.empee.mysticalbarriers.handlers;

import io.papermc.paper.event.entity.EntityMoveEvent;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.config.BarriersConfig;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import ml.empee.mysticalbarriers.utils.PaperUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.List;

/**
 * Prevent a player from bypassing a barrier
 */

@RequiredArgsConstructor
public class BarrierAccessHandler implements Bean, RegisteredListener {

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
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onVehicleMove(VehicleMoveEvent event) {
    if (LocationUtils.isSameBlock(event.getFrom(), event.getTo())) {
      return;
    } else if (barrierService.findBarrierByBlock(event.getTo().getBlock()).isEmpty()) {
      return;
    }

    Entity vehicle = event.getVehicle();

    vehicle.eject();
    vehicle.teleport(event.getFrom());
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

  /**
   * Prevents player from entering a vehicle inside a barrier
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPlayerEnterVehicle(VehicleEnterEvent event) {
    Barrier barrier = barrierService.findBarrierByBlock(
        event.getVehicle().getLocation().getBlock()
    ).orElse(null);

    if (barrier == null) {
      return;
    } else if (event.getEntered() instanceof Player player) {
      if (!barrier.isVisibleFor(player)) {
        return;
      }
    }

    event.setCancelled(true);
  }

  /**
   * Listeners registered only if the server is running paper
   */
  @RequiredArgsConstructor
  public static class PaperListeners implements Bean, RegisteredListener {
    private final BarrierService barrierService;

    @Override
    public boolean isEnabled() {
      return PaperUtils.IS_RUNNING_PAPER;
    }

    /**
     * Prevents entities from going into a barrier
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityMove(EntityMoveEvent event) {
      if (LocationUtils.isSameBlock(event.getFrom(), event.getTo())) {
        return;
      }

      Barrier barrier = barrierService.findBarrierByBlock(event.getTo().getBlock()).orElse(null);
      if (barrier == null) {
        return;
      }

      event.setCancelled(true);
    }
  }

}
