package core.importers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import core.model.Barrier;
import core.repositories.nitrite.BarriersRepository;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;
import utils.Messenger;
import utils.regions.CubicRegion;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Map;

@LightWired
public class JsonBarriersImporter {

  private final Gson gson = new Gson();
  private final BarriersRepository barriersRepository;

  public JsonBarriersImporter(JavaPlugin plugin, BarriersRepository barriersRepository) {
    this.barriersRepository = barriersRepository;

    var target = new File(plugin.getDataFolder(), "barriers.json");
    if (target.exists()) {
      importJson(target);
    }
  }

  @SneakyThrows
  public void importJson(File file) {
    Messenger.log("Started import of json barrier file {}", file);

    Map[] barriers = gson.fromJson(new JsonReader(new FileReader(file)), Map[].class);
    for (var barrier : barriers) {
      try {
        barriersRepository.save(convertBarrier(barrier));
      } catch (Exception e) {
        Messenger.error("Error while parsing barrier", e);
      }
    }

    Files.move(file.toPath(), file.toPath().getParent().resolve("barriers.json.bk"));
    Messenger.log("Finished import of json barrier file {}", file);
  }

  private Barrier convertBarrier(Map map) {
    Location start = convertLocation((Map) map.get("first_corner"));
    Location end = convertLocation((Map) map.get("second_corner"));

    Material material = Material.valueOf((String) map.get("material"));
    String data = (String) map.get("block_data");

    return Barrier.builder()
        .id((String) map.get("id"))
        .activationRange(((Double) map.get("activation_range")).intValue())
        .region(CubicRegion.of(start, end))
        .fillBlock(data != null ? buildBlockData(material, data) : material.createBlockData())
        .build();
  }

  private BlockData buildBlockData(Material material, String data) {
    try {
      return material.createBlockData(data);
    } catch (IllegalArgumentException var4) {
      try {
        String wallData = data.replace("true", "tall").replace("false", "none");
        return material.createBlockData(wallData);
      } catch (IllegalArgumentException var3) {
        return null;
      }
    }
  }

  private Location convertLocation(Map map) {
    String world = (String) map.get("world");
    double x = (Double) map.get("x");
    double y = (Double) map.get("y");
    double z = (Double) map.get("z");

    if (Bukkit.getWorld(world) == null) {
      throw new IllegalArgumentException("World " + world + " not found");
    }

    return new Location(Bukkit.getWorld(world), x, y, z);
  }

}
