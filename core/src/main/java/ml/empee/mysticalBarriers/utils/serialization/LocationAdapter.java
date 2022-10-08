package ml.empee.mysticalBarriers.utils.serialization;

import java.lang.reflect.Type;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

final class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

  @Override
  public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    if(src.getWorld() != null) {
      object.addProperty("world", src.getWorld().getName());
    }

    object.addProperty("x", src.getX());
    object.addProperty("y", src.getY());
    object.addProperty("z", src.getZ());
    object.addProperty("yaw", src.getYaw());
    object.addProperty("pitch", src.getPitch());
    return object;
  }

  @Override
  public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws
      JsonParseException {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    return new Location(
        Bukkit.getWorld(jsonObject.get("world").getAsString()),
        jsonObject.get("x").getAsDouble(),
        jsonObject.get("y").getAsDouble(),
        jsonObject.get("z").getAsDouble(),
        jsonObject.get("yaw").getAsFloat(),
        jsonObject.get("pitch").getAsFloat()
    );
  }
}