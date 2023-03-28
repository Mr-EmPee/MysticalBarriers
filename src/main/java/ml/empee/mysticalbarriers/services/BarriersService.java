package ml.empee.mysticalbarriers.services;

import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.model.adapters.MultiBlockPacket;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.model.exceptions.MysticalBarrierException;
import ml.empee.mysticalbarriers.model.mappers.BarrierMapper;
import ml.empee.mysticalbarriers.utils.helpers.JsonPersistence;
import ml.empee.mysticalbarriers.utils.helpers.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BarriersService implements Bean {

  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(BarriersService.class);
  private final JsonPersistence jsonPersistence = new JsonPersistence(
    new File(plugin.getDataFolder(), "barriers.json")
  );

  private final Logger logger;
  private List<Barrier> barriers;

  public void onStart() {
    BarrierMapper.mapToLatest(jsonPersistence.getFile());

    barriers = jsonPersistence.deserializeList(Map[].class).stream()
      .map(Barrier::fromMap)
      .collect(Collectors.toCollection(ArrayList::new));

    logger.info("Loaded " + barriers.size() + " barriers");
    refreshAllBarriers();
  }

  public void onStop() {
    hideAllBarriers();
  }

  private void saveAsyncBarriers() {
    List<Map<String, Object>> barriers = this.barriers.stream()
      .map(Barrier::toMap)
      .toList();

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      jsonPersistence.serialize(barriers);
    });
  }

  public void saveBarrier(Barrier barrier) {
    if (barriers.contains(barrier)) {
      return;
    }

    barriers.add(barrier);
    refreshBarrier(barrier);
    saveAsyncBarriers();
  }

  public void updateBarrier(Barrier barrier) {
    if (!barriers.contains(barrier)) {
      return;
    }

    refreshBarrier(barrier);
    saveAsyncBarriers();
  }

  public boolean removeBarrier(Barrier barrier) {
    if (!barriers.contains(barrier)) {
      return false;
    }

    barriers.remove(barrier);
    hideBarrier(barrier);
    saveAsyncBarriers();
    return true;
  }

  public List<Barrier> findAllBarriers() {
    return Collections.unmodifiableList(barriers);
  }

  @Nullable
  public Barrier findBarrierAt(Location location) {
    return barriers.stream()
      .filter(barrier -> barrier.isBarrierAt(location))
      .findFirst()
      .orElse(null);
  }

  /**
   * @param range if not specified will use the barrier range
   */
  public List<Barrier> findBarriersWithinRangeAt(Location location, @Nullable Integer range) {
    return barriers.stream()
      .filter(barrier -> barrier.isWithinRange(location, range))
      .collect(Collectors.toList());
  }

  @Nullable
  public Barrier findBarrierByID(String id) {
    return barriers.stream()
      .filter(barrier -> barrier.getId().equals(id))
      .findFirst()
      .orElse(null);
  }

  public void refreshBarrierFor(Player player, Barrier barrier) {
    if (barrier.isHiddenFor(player)) {
      hideBarrierTo(player, barrier);
    } else {
      showBarrierFor(player, barrier);
    }
  }

  public void refreshBarrier(Barrier barrier) {
    Bukkit.getOnlinePlayers().forEach(player -> refreshBarrierFor(player, barrier));
  }

  public void refreshAllBarriers() {
    barriers.forEach(this::refreshBarrier);
  }

  public void hideAllBarriers() {
    barriers.forEach(this::hideBarrier);
  }

  public void hideBarrier(Barrier barrier) {
    Bukkit.getOnlinePlayers().forEach(player -> hideBarrierTo(player, barrier));
  }

  public void hideBarrierTo(Player player, Barrier barrier) {
    if (!player.getWorld().equals(barrier.getWorld())) {
      return;
    }

    hideBarrierAt(player.getLocation(), barrier, player);
  }

  public void showBarrier(Barrier barrier) {
    Bukkit.getOnlinePlayers().forEach(player -> showBarrierFor(player, barrier));
  }

  public void showBarrierFor(Player player, Barrier barrier) {
    if (!player.getWorld().equals(barrier.getWorld())) {
      return;
    }

    showBarrierAt(player.getLocation(), barrier, player);
  }

  public void hideBarrierAt(Location location, Barrier barrier, Player player) {
    MultiBlockPacket packet = new MultiBlockPacket(location, false);
    barrier.forEachVisibleBarrierBlock(location, loc -> packet.addBlock(Material.AIR, loc));
    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException(
        "Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e
      );
    }
  }

  public void showBarrierAt(Location location, Barrier barrier, Player player) {
    MultiBlockPacket packet = new MultiBlockPacket(location, false);
    barrier.forEachVisibleBarrierBlock(location, loc ->
      packet.addBackwardProofBlock(barrier.getMaterial(), null, barrier.getBlockData(), loc)
    );

    try {
      packet.send(player);
    } catch (InvocationTargetException e) {
      throw new MysticalBarrierException(
        "Error while refreshing the barrier " + barrier.getId() + " for " + player.getName(), e
      );
    }
  }

}
