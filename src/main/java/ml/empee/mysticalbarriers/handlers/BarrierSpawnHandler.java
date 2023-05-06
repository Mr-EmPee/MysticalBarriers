package ml.empee.mysticalbarriers.handlers;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.ioc.ScheduledTask;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.services.BarrierService;
import ml.empee.mysticalbarriers.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>Handler used to spawn the barrier blocks near a player.</p>
 * <p>This is a scheduled task due some factors</p>
 * <ul>
 *   <li>It doesn't exists an event that notify the plugin when the user permissions changes</li>
 *   <li>Horse movement bugged (When jumping it doesn't call playerMoveEv nor VehicleMovEv)</li>
 *   <li>PlayerMovEv isn't called when riding Llamas or mine-carts (It exists a workaround)</li>
 *   <li>Player when join the server hasn't yet loaded the chunks (It exists a paper-event)</li>
 * </ul>
 */

public class BarrierSpawnHandler extends ScheduledTask implements Bean, RegisteredListener {

  private static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(BarrierSpawnHandler.class);
  private final PacketAdapter serverBlockChangeListener = new PacketAdapter(plugin, Server.BLOCK_CHANGE) {
    @Override
    public void onPacketSending(PacketEvent event) {
      onServerSendBlockChange(event);
    }
  };

  private final BarrierService barrierService;
  private final Cache<Player, PlayerData> lastLocations = CacheBuilder.newBuilder()
      .expireAfterAccess(10, TimeUnit.SECONDS)
      .build();

  @Data
  private static class PlayerData {
    private Location lastLocation;
    private Set<Barrier> lastSeen = new HashSet<>();
  }

  public BarrierSpawnHandler(BarrierService barrierService) {
    super(0, 6, false);

    this.barrierService = barrierService;
  }

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

    Vector distance = LocationUtils.getDistance(player.getLocation(), block.getLocation());
    double maxDistance = Math.max(distance.getX(), Math.max(distance.getY(), distance.getZ()));
    if (maxDistance > barrier.getActivationRange()) {
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

  @Override
  @SneakyThrows
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      var data = lastLocations.get(player, PlayerData::new);

      refreshBarriers(player, data);

      data.setLastLocation(player.getLocation());
    }
  }

  private void refreshBarriers(Player player, PlayerData data) {
    //TODO: Improve performance
    Location currentLoc = player.getLocation();
    if (data.getLastLocation() != null) {
      if (LocationUtils.isSameBlock(currentLoc, data.getLastLocation())) {
        return;
      }

      data.getLastSeen().forEach(b -> {
        b.hideBarrier(player, data.getLastLocation());
      });
    }

    data.getLastSeen().clear();

    barrierService.findBarrierNear(currentLoc).forEach(b -> {
      if (b.isVisibleFor(player)) {
        b.showBarrier(player, currentLoc);
        data.getLastSeen().add(b);
      }
    });
  }

}
