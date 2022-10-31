package ml.empee.mysticalBarriers.services;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.utils.ArrayUtils;
import ml.empee.mysticalBarriers.utils.FileSaver;
import ml.empee.mysticalBarriers.utils.Logger;
import ml.empee.mysticalBarriers.utils.serialization.SerializationUtils;

public class BarriersService extends AbstractService {

  private static final String FILE_NAME = "barriers.json";

  private final Set<Barrier> barriers = ConcurrentHashMap.newKeySet();
  private final AtomicBoolean savingScheduled;

  public BarriersService() {
    barriers.addAll( ArrayUtils.toList(SerializationUtils.deserialize(FILE_NAME, Barrier[].class)) );
    Logger.info("Loaded " + barriers.size() + " barriers");

    savingScheduled = FileSaver.scheduleSaving(barriers, FILE_NAME);

    for(Player player : Bukkit.getOnlinePlayers()) {
      for (Barrier barrier : barriers) {
        refreshBarrierFor(player, barrier);
      }
    }
  }

  @Override
  protected void onDisable() {
    for(Player player : Bukkit.getOnlinePlayers()) {
      for (Barrier barrier : barriers) {
        hideBarrierTo(player, barrier);
      }
    }
  }

  public boolean saveBarrier(Barrier barrier) {
    if(barriers.add(barrier)) {
      savingScheduled.set(true);
      return true;
    }

    return false;
  }
  public boolean updateBarrier(Barrier barrier) {
    if(barriers.remove(barrier)) {
      barriers.add(barrier);

      for(Player player : Bukkit.getOnlinePlayers()) {
        if(player.getWorld().equals(barrier.getWorld())) {
          refreshBarrierFor(player, barrier);
        }
      }

      savingScheduled.set(true);
      return true;
    }

    return false;
  }
  public Set<Barrier> findAllBarriers() {
    return Collections.unmodifiableSet(barriers);
  }
  public Barrier findBarrierAt(Location location) {
    for(Barrier barrier : barriers) {
      if(barrier.isBarrierBlock(location)) {
        return barrier;
      }
    }

    return null;
  }
  @Nullable
  public Barrier findBarrierByID(String id) {
    for(Barrier barrier : barriers) {
      if(barrier.getId().equals(id)) {
        return barrier;
      }
    }

    return null;
  }
  public boolean removeBarrier(Barrier barrier) {
    if(barriers.remove(barrier)) {
      for(Player player : Bukkit.getOnlinePlayers()) {
        if(player.getWorld().equals(barrier.getWorld()) && !barrier.isHiddenFor(player)) {
          hideBarrierTo(player, barrier);
        }
      }

      savingScheduled.set(true);
      return true;
    }

    return false;
  }

  public void refreshBarrierFor(Player player, Barrier barrier) {
    if(barrier.isHiddenFor(player)) {
      hideBarrierTo(player, barrier);
    } else {
      showBarrierTo(player, barrier);
    }
  }

  public void hideBarrierTo(Player player, Barrier barrier) {
    MultiBlockPacket packet = new MultiBlockPacket(player.getLocation(), false);
    barrier.forEachVisibleBarrierBlock(player.getLocation(), (x, y, z) -> packet.addBlock(Material.AIR, x, y, z));
    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException("Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e);
    }
  }

  public void showBarrierTo(Player player, Barrier barrier) {
    MultiBlockPacket packet = new MultiBlockPacket(player.getLocation(), false);
    barrier.forEachVisibleBarrierBlock(player.getLocation(), (x, y, z) -> packet.addBlock(barrier.getMaterial(), x, y, z));
    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException("Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e);
    }
  }

}
