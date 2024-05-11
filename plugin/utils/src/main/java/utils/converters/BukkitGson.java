package utils.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Type;

/**
 * Utility class to convert bukkit data types
 */

@UtilityClass
public class BukkitGson {

  private static final Gson GSON = gson().create();

  public <T, I> T convert(I input, Class<T> output) {
    return GSON.fromJson(GSON.toJson(input), output);
  }

  public GsonBuilder gson() {
    var gson = new GsonBuilder();

    gson.disableHtmlEscaping();

    gson.registerTypeHierarchyAdapter(BlockData.class, new BlockDataConverter());
    gson.registerTypeAdapter(Location.class, new LocationConverter());

    return gson;
  }

  private static class LocationConverter implements JsonSerializer<Location>, JsonDeserializer<Location> {
    @Override
    public JsonElement serialize(
        Location location, Type type, JsonSerializationContext jsonSerializationContext
    ) {
      JsonObject obj = new JsonObject();
      if (location.getWorld() != null) {
        obj.addProperty("world", location.getWorld().getName());
      }

      obj.addProperty("x", location.getX());
      obj.addProperty("y", location.getY());
      obj.addProperty("z", location.getZ());

      obj.addProperty("yaw", location.getYaw());
      obj.addProperty("pitch", location.getPitch());

      return obj;
    }

    @Override
    public Location deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext
    ) throws JsonParseException {
      JsonObject obj = (JsonObject) jsonElement;

      double x = obj.get("x").getAsDouble();
      double y = obj.get("y").getAsDouble();
      double z = obj.get("z").getAsDouble();

      float yaw = obj.get("yaw").getAsFloat();
      float pitch = obj.get("pitch").getAsFloat();

      var loc = new Location(null, x, y, z, yaw, pitch);
      if (obj.has("world")) {
        loc.setWorld(Bukkit.getWorld(obj.get("world").getAsString()));
      }

      return loc;
    }
  }

  private static class BlockDataConverter implements JsonSerializer<BlockData>, JsonDeserializer<BlockData> {
    @Override
    public JsonElement serialize(
        BlockData block, Type type, JsonSerializationContext jsonSerializationContext
    ) {
      return new JsonPrimitive(block.getAsString());
    }

    @Override
    public BlockData deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext
    ) throws JsonParseException {
      return Bukkit.createBlockData(jsonElement.getAsString());
    }
  }

}
