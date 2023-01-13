package ml.empee.mysticalBarriers.services;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import ml.empee.ioc.Stoppable;
import ml.empee.ioc.annotations.Bean;
import ml.empee.json.JsonRepository;
import ml.empee.mysticalBarriers.exceptions.MysticalBarrierException;
import ml.empee.mysticalBarriers.model.Barrier;
import ml.empee.mysticalBarriers.model.packets.MultiBlockPacket;
import ml.empee.mysticalBarriers.utils.MCLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Bean
public class BarriersService implements Stoppable {
  private final JsonRepository<Barrier> repo;

  public BarriersService() {
    repo = new JsonRepository<>("barriers.json", Barrier[].class);
    MCLogger.info("Loaded " + repo.size() + " barriers");
    refreshAllBarriers();
  }

  @Override
  public void stop() {
    hideAllBarriers();
  }

  public boolean saveBarrier(Barrier barrier) {
    if (!repo.contains(barrier)) {
      repo.save(barrier);
      refreshBarrier(barrier);
      return true;
    }

    return false;
  }
  public boolean updateBarrier(Barrier barrier) {
    if (repo.contains(barrier)) {
      repo.saveDB();
      refreshBarrier(barrier);
      return true;
    }

    return false;
  }
  public boolean removeBarrier(Barrier barrier) {
    if (repo.contains(barrier)) {
      repo.remove(barrier);
      hideBarrier(barrier);
      return true;
    }

    return false;
  }

  public List<Barrier> findAllBarriers() {
    return repo.findAll();
  }
  @Nullable
  public Barrier findBarrierAt(Location location) {
    return repo.stream()
        .filter(barrier -> barrier.isBarrierAt(location))
        .findFirst()
        .orElse(null);
  }
  /**
   * @param range if not specified will use the barrier range
   */
  public List<Barrier> findBarriersWithinRangeAt(Location location, @Nullable Integer range) {
    return repo.stream()
        .filter(barrier -> barrier.isWithinRange(location, range))
        .collect(Collectors.toList());
  }
  @Nullable
  public Barrier findBarrierByID(String id) {
    return repo.stream()
        .filter(barrier -> barrier.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  public void refreshBarrierFor(Player player, Barrier barrier) {
    if (barrier.isHiddenFor(player)) {
      hideBarrierTo(player, barrier);
    } else {
      showBarrierTo(player, barrier);
    }
  }
  public void refreshBarrier(Barrier barrier) {
    Bukkit.getOnlinePlayers().forEach(player -> refreshBarrierFor(player, barrier));
  }
  public void refreshAllBarriers() {
    repo.forEach(this::refreshBarrier);
  }

  public void hideAllBarriers() {
    repo.forEach(this::hideBarrier);
  }
  public void hideBarrier(Barrier barrier) {
    Bukkit.getOnlinePlayers().forEach(player -> hideBarrierTo(player, barrier));
  }
  public void hideBarrierTo(Player player, Barrier barrier) {
    if(!player.getWorld().equals(barrier.getWorld())) {
      return;
    }

    despawnBarrierAt(player.getLocation(), barrier, player);
  }

  public void showBarrier(Barrier barrier) {
    Bukkit.getOnlinePlayers().forEach(player -> showBarrierTo(player, barrier));
  }
  public void showBarrierTo(Player player, Barrier barrier) {
    if(!player.getWorld().equals(barrier.getWorld())) {
      return;
    }

    spawnBarrierAt(player.getLocation(), barrier, player);
  }

  public void despawnBarrierAt(Location location, Barrier barrier, Player player) {
    MultiBlockPacket packet = new MultiBlockPacket(location, false);
    barrier.forEachVisibleBarrierBlock(location, loc -> packet.addBlock(Material.AIR, loc));
    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException(
          "Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e);
    }
  }
  public void spawnBarrierAt(Location location, Barrier barrier, Player player) {
    MultiBlockPacket packet = new MultiBlockPacket(location, false);
    barrier.forEachVisibleBarrierBlock(location, loc ->
        packet.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), loc)
    );

    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException(
          "Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e);
    }
  }

}
