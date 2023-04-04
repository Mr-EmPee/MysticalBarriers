package ml.empee.mysticalbarriers.handlers;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import ml.empee.mysticalbarriers.utils.PaperUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handler used to spawn the barrier blocks near a player
 */

@RequiredArgsConstructor
public class BarrierSpawnHandler implements Bean, RegisteredListener {

  private static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(BarrierSpawnHandler.class);
  private final PacketAdapter serverBlockChangeListener = new PacketAdapter(plugin, Server.BLOCK_CHANGE) {
    @Override
    public void onPacketSending(PacketEvent event) {
      onServerSendBlockChange(event);
    }
  };

  private final BarrierService barrierService;

  @Override
  public void onStart() {
    PROTOCOL_MANAGER.addPacketListener(serverBlockChangeListener);
  }

  @Override
  public void onStop() {
    PROTOCOL_MANAGER.removePacketListener(serverBlockChangeListener);
  }

  /**
   * Re-send barrier blocks when a player breaks it
   */
  @SneakyThrows
  public void onServerSendBlockChange(PacketEvent event) {
    Player player = event.getPlayer();
    PacketContainer packet = event.getPacket();
    Block block = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld()).getBlock();
    Barrier barrier = barrierService.findBarrierByBlock(block).orElse(null);
    if (barrier == null || !barrier.isVisibleFor(player)) {
      return;
    }

    int distance = LocationUtils.getGreatestAxisDistance(player.getLocation(), block.getLocation());
    if (distance > barrier.getActivationRange()) {
      return;
    }

    Material updateMaterial = packet.getBlockData().read(0).getType();
    if (!updateMaterial.name().contains("AIR")) {
      return;
    }

    packet = packet.deepClone();
    packet.getBlockData().write(0, WrappedBlockData.createData(barrier.getBlockData()));

    event.setCancelled(true);
    PROTOCOL_MANAGER.sendServerPacket(player, packet, true);
  }

  /**
   * Send barrier blocks when a player move
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (LocationUtils.isSameBlock(event.getFrom(), event.getTo())) {
      return;
    }

    Player player = event.getPlayer();

    //TODO Improve performance
    for (Barrier barrier : barrierService.findAll()) {
      if (barrier.isNear(event.getFrom())) {
        barrier.hideBarrier(player, event.getFrom());
      }

      if (barrier.isNear(event.getTo()) && barrier.isVisibleFor(player)) {
        barrier.showBarrier(player, event.getTo());
      }
    }

  }

  /**
   * Prevent players from breaking a barrier block
   */
  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onPlayerBreak(BlockBreakEvent event) {
    Barrier barrier = barrierService.findBarrierByBlock(event.getBlock()).orElse(null);
    if (barrier == null || !barrier.isVisibleFor(event.getPlayer())) {
      return;
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
     * Spawn barrier blocks when a player join near a barrier
     */
    @EventHandler
    public void onPlayerLoadChunk(PlayerChunkLoadEvent event) {
      Player player = event.getPlayer();
      for (Barrier barrier : barrierService.findBarrierNear(player.getLocation())) {
        if (barrier.isVisibleFor(player)) {
          barrier.showBarrier(player, player.getLocation());
        }
      }
    }
  }

}
