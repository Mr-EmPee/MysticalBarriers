package ml.empee.mysticalbarriers.repositories;

import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.helpers.JsonPersistence;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persistence layer for barriers
 */

public class BarrierRepository implements Bean {

  private final JsonPersistence repo;
  private final JavaPlugin plugin;
  private Set<Barrier> barriers;

  public BarrierRepository(JavaPlugin plugin) {
    this.repo = new JsonPersistence(new File(plugin.getDataFolder(), "barriers.json"));
    this.plugin = plugin;

    loadBarriers();
  }

  public void loadBarriers() {
    barriers = repo.deserializeList(Map[].class).stream()
      .map(Barrier::fromMap)
      .collect(Collectors.toCollection(HashSet::new));

    Logger.info("Loaded " + barriers.size() + " barriers");
  }

  public void save(Barrier barrier) {
    barriers.add(barrier);
    saveAsync();
  }

  public void delete(Barrier barrier) {
    barriers.remove(barrier);
    saveAsync();
  }

  public Optional<Barrier> findById(String id) {
    return barriers.stream()
      .filter(b -> b.getId().equals(id))
      .findFirst();
  }

  private void saveAsync() {
    List<Map<String, Object>> barriers = this.barriers.stream()
        .map(Barrier::toMap)
        .toList();

    Bukkit.getScheduler().runTaskAsynchronously(
        plugin, () -> repo.serialize(barriers)
    );
  }
}
