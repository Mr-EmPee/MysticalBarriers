package ml.empee.mysticalBarriers.controllers.commands.listeners;

import lombok.RequiredArgsConstructor;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.Logger;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerContext;
import ml.empee.mysticalBarriers.utils.helpers.cache.PlayerData;
import ml.empee.mysticalBarriers.utils.nms.ServerVersion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor
public class BarrierDefiner implements Listener {

  private final PlayerContext<Barrier> barrierCreationContext = PlayerContext.get("barrierCreationContext");

  private final BarriersService barriersService;

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if(event.getClickedBlock() == null) {
      return;
    }

    PlayerData<Barrier> playerData = barrierCreationContext.get(event.getPlayer());
    if (playerData == null) {
      return;
    }

    event.setCancelled(true);
    if (ServerVersion.isGreaterThan(1, 9) && EquipmentSlot.OFF_HAND == event.getHand()) {
      return;
    }

    onValidClick(event, playerData.get());
  }

  private void onValidClick(PlayerInteractEvent event, Barrier barrier) {
    Player player = event.getPlayer();
    if (event.getAction().name().contains("LEFT")) {
      barrierCreationContext.remove(player);
      Logger.info(player, "Barrier creation mode disabled!");
    } else {
      if (event.getClickedBlock() != null) {
        defineBarrier(event, barrier);
      }
    }
  }

  private void defineBarrier(PlayerInteractEvent event, Barrier barrier) {
    Block clickedBlock = event.getClickedBlock();
    Player player = event.getPlayer();

    if (barrier.getFirstCorner() == null) {
      barrier.setFirstCorner(clickedBlock.getLocation());
      Logger.info(player, "You selected the first corner");
    } else {
      barrierCreationContext.remove(player);
      barrier.setSecondCorner(clickedBlock.getLocation());

      if(barriersService.saveBarrier(barrier)) {
        Logger.info(player, "The barrier '&e%s&r' has been created!", barrier.getId());
      } else {
        Logger.error(player, "A barrier named '&e%s&r' already exists!", barrier.getId());
      }
    }

  }
}
