package ml.empee.mysticalBarriers.services;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.utils.ArrayUtils;
import ml.empee.mysticalBarriers.utils.Logger;
import ml.empee.mysticalBarriers.utils.serialization.PersistenceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BarriersService extends AbstractService {

  private static final String FILE_NAME = "barriers.json";

  private final Set<Barrier> barriers = ConcurrentHashMap.newKeySet();

  public BarriersService() {
    barriers.addAll(ArrayUtils.toList(PersistenceUtils.deserialize(FILE_NAME, Barrier[].class)));
    Logger.info("Loaded " + barriers.size() + " barriers");

    for (Player player : Bukkit.getOnlinePlayers()) {
      for (Barrier barrier : barriers) {
        refreshBarrierFor(player, barrier);
      }
    }
  }

  @Override
  protected void onDisable() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      for (Barrier barrier : barriers) {
        hideBarrierTo(player, barrier);
      }
    }
  }

  private void saveFile() {
    PersistenceUtils.serializeAsync(barriers, FILE_NAME);
  }

  public boolean saveBarrier(Barrier barrier) {
    if (barriers.add(barrier)) {
      saveFile();

      for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getWorld().equals(barrier.getWorld())) {
          refreshBarrierFor(player, barrier);
        }
      }

      return true;
    }

    return false;
  }

  public boolean updateBarrier(Barrier barrier) {
    if (barriers.remove(barrier)) {
      barriers.add(barrier);
      saveFile();

      for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getWorld().equals(barrier.getWorld())) {
          refreshBarrierFor(player, barrier);
        }
      }

      return true;
    }

    return false;
  }

  public Set<Barrier> findAllBarriers() {
    return Collections.unmodifiableSet(barriers);
  }

  public Barrier findBarrierAt(Location location) {
    for (Barrier barrier : barriers) {
      if (barrier.existsBarrierAt(location)) {
        return barrier;
      }
    }

    return null;
  }

  /**
   * @param range if not specified will use the barrier range
   */
  public List<Barrier> findBarriersWithinRangeAt(Location location, @Nullable Integer range) {
    ArrayList<Barrier> barriers = new ArrayList<>();

    for (Barrier barrier : this.barriers) {
      if (barrier.isWithinRange(location, range)) {
        barriers.add(barrier);
      }
    }

    return barriers;
  }

  @Nullable
  public Barrier findBarrierByID(String id) {
    for (Barrier barrier : barriers) {
      if (barrier.getId().equals(id)) {
        return barrier;
      }
    }

    return null;
  }

  public boolean removeBarrier(Barrier barrier) {
    if (barriers.remove(barrier)) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.getWorld().equals(barrier.getWorld()) && !barrier.isHiddenFor(player)) {
          hideBarrierTo(player, barrier);
        }
      }

      saveFile();
      return true;
    }

    return false;
  }

  public void refreshBarrierFor(Player player, Barrier barrier) {
    if (barrier.isHiddenFor(player)) {
      hideBarrierTo(player, barrier);
    } else {
      showBarrierTo(player, barrier);
    }
  }

  public void despawnBarrierAt(Location location, Barrier barrier, Player player) {
    MultiBlockPacket packet = new MultiBlockPacket(location, false);
    barrier.forEachVisibleBarrierBlock(location,
        (x, y, z) -> packet.addBlock(Material.AIR, x, y, z));
    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException(
          "Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e);
    }
  }

  public void hideBarrierTo(Player player, Barrier barrier) {
    despawnBarrierAt(player.getLocation(), barrier, player);
  }

  public void showBarrierTo(Player player, Barrier barrier) {
    spawnBarrierAt(player.getLocation(), barrier, player);
  }

  public void spawnBarrierAt(Location location, Barrier barrier, Player player) {
    MultiBlockPacket packet = new MultiBlockPacket(location, false);
    barrier.forEachVisibleBarrierBlock(location, (x, y, z) -> {
      packet.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), x, y, z);
    });
    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException(
          "Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e);
    }
  }

}
