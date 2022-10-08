package ml.empee.mysticalBarriers.controllers.components;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import lombok.RequiredArgsConstructor;
import ml.empee.mysticalBarriers.helpers.PlayerContext;
import ml.empee.mysticalBarriers.helpers.Tuple;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.services.BarriersService;
import ml.empee.mysticalBarriers.utils.Logger;

@RequiredArgsConstructor
public class BarrierDefiner implements Listener {

  private final PlayerContext<Tuple<String, Location>> targetedPlayers = PlayerContext.get("barrierCreation");

  private final BarriersService barriersService;

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerInteract(PlayerInteractEvent event) {

    targetedPlayers.iterate((iterator, player, tuple) -> {
      if (!player.equals(event.getPlayer())) {
        return;
      }
      event.setCancelled(true);
      if (EquipmentSlot.OFF_HAND == event.getHand()) {
        return;
      }

      onValidClick(event, iterator, player, tuple);
    });

  }

  private void onValidClick(
      PlayerInteractEvent event, PlayerContext.PlayerIterator<Tuple<String, Location>> iterator,
      Player player, Tuple<String, Location> tuple
  ) {

    if (event.getAction().name().contains("LEFT")) {
      iterator.remove();
      Logger.info(player, "Barrier creation mode disabled!");
    } else {
      if (event.getClickedBlock() != null) {
        defineBarrier(iterator, player, tuple, event.getClickedBlock());
      }
    }

  }

  private void defineBarrier(
      PlayerContext.PlayerIterator<Tuple<String, Location>> iterator,
      Player player, Tuple<String, Location> tuple, Block clickedBlock
  ) {

    if (tuple.getSecondValue() == null) {
      tuple.setSecondValue(clickedBlock.getLocation());
      Logger.info(player, "You selected the first corner");
    } else {
      iterator.remove();

      if (
          barriersService.saveBarrier(
              Barrier.builder()
                  .id(tuple.getFirstValue())
                  .firstCorner(tuple.getSecondValue())
                  .secondCorner(clickedBlock.getLocation())
                  .build()
          )
      ) {
        Logger.info(player, "The barrier '&e%s&r' has been created!", tuple.getFirstValue());
      } else {
        Logger.error(player, "A barrier named '&e%s&r' already exists!", tuple.getFirstValue());
      }
    }

  }

}
