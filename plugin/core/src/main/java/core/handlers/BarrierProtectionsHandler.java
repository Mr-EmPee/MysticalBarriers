package core.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import core.model.Barrier;
import core.services.BarriersService;
import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

@LightWired
public class BarrierProtectionsHandler extends PacketAdapter implements Listener {

  private final BarriersService barriersService;

  public BarrierProtectionsHandler(JavaPlugin plugin, BarriersService barriersService) {
    super(plugin, PacketType.Play.Server.BLOCK_CHANGE);

    this.barriersService = barriersService;
  }

  @EventHandler
  public void onBarrierBlockPlace(BlockPlaceEvent event) {
    var block = event.getBlock();
    var player = event.getPlayer();
    var barrier = barriersService.findBarrierAt(block.getLocation()).orElse(null);
    if (barrier == null || barriersService.isHidden(barrier, player)) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onBarrierBlockBreak(BlockBreakEvent event) {
    var block = event.getBlock();
    var player = event.getPlayer();
    var barrier = barriersService.findBarrierAt(block.getLocation()).orElse(null);
    if (barrier == null || barriersService.isHidden(barrier, player)) {
      return;
    }

    event.setCancelled(true);
  }

  @Override
  public void onPacketSending(PacketEvent event) {
    var packet = event.getPacket();
    var player = event.getPlayer();

    Location location = packet.getBlockPositionModifier().read(0).toLocation(player.getWorld());
    Block block = location.getBlock();
    if (block.getType() != Material.AIR) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(location).orElse(null);
    if (barrier == null) {
      return;
    }

    if (barriersService.isHidden(barrier, player)) {
      return;
    }

    packet.getBlockData().write(0, WrappedBlockData.createData(barrier.getBlockAt(location)));
  }

}
