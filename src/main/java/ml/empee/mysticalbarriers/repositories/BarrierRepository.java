package ml.empee.mysticalbarriers.repositories;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.helpers.JsonPersistence;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
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

  private static final int LATEST_VERSION = 2;
  private final JsonPersistence repo;
  private final JavaPlugin plugin;
  private Set<Barrier> barriers;

  public BarrierRepository(JavaPlugin plugin) {
    this.repo = new JsonPersistence(new File(plugin.getDataFolder(), "barriers.json"));
    this.plugin = plugin;

    update();

    loadBarriers();
  }

  @SneakyThrows
  private void update() {
    JsonArray barriers = repo.deserialize(JsonArray.class);
    if (barriers == null) {
      return;
    }

    boolean hasBeenUpdated = false;
    for (JsonElement barrier : barriers) {
      int version = Optional.ofNullable(
          barrier.getAsJsonObject().get("version")
      ).map(JsonElement::getAsInt).orElse(1);

      if (version == LATEST_VERSION) {
        continue;
      }

      updateToV2(version, barrier.getAsJsonObject());
      hasBeenUpdated = true;
    }

    if (hasBeenUpdated) {
      File file = repo.getFile();
      Files.move(
          file.toPath(), new File(file.getParent(), file.getName() + ".old").toPath()
      );

      repo.serialize(barriers);
    }
  }

  private void updateToV2(int version, JsonObject element) {
    if (version >= 2) {
      return;
    }

    element.addProperty("version", 2);

    JsonObject corner = element.getAsJsonObject("first_corner");
    String converterCorner = corner.getAsJsonPrimitive("world").getAsString()
        + ":" + ((int) corner.getAsJsonPrimitive("x").getAsDouble())
        + ":" + ((int) corner.getAsJsonPrimitive("y").getAsDouble())
        + ":" + ((int) corner.getAsJsonPrimitive("z").getAsDouble());

    element.addProperty("first_corner", converterCorner);

    corner = element.getAsJsonObject("second_corner");
    converterCorner = corner.getAsJsonPrimitive("world").getAsString()
        + ":" + ((int) corner.getAsJsonPrimitive("x").getAsDouble())
        + ":" + ((int) corner.getAsJsonPrimitive("y").getAsDouble())
        + ":" + ((int) corner.getAsJsonPrimitive("z").getAsDouble());

    element.addProperty("second_corner", converterCorner);

    String blockData = element.getAsJsonPrimitive("material").getAsString().toLowerCase();
    blockData = "minecraft:" + blockData;
    blockData += element.getAsJsonPrimitive("block_data").getAsString().toLowerCase();
    element.addProperty("block_data", blockData);
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

  public Set<Barrier> findAll() {
    return Collections.unmodifiableSet(barriers);
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
