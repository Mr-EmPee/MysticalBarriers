package ml.empee.mysticalbarriers.model.mappers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import ml.empee.mysticalbarriers.utils.helpers.JsonPersistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class BarrierMapper {

  public static void mapToLatest(File input) {
    JsonPersistence gson = new JsonPersistence(input);
    JsonArray barriers = gson.deserialize(JsonArray.class);
    if (barriers == null) {
      return;
    }

    boolean dirty = false;
    dirty |= mapV0ToV1(barriers);

    if(dirty) {
      markAsOld(input);
      gson.serialize(barriers);
    }
  }

  @SneakyThrows
  private static void markAsOld(File input) {
    File old = new File(input.getParentFile(), input.getName() + ".old");
    old.delete();

    Files.move(input.toPath(), old.toPath());
  }

  public static boolean mapV0ToV1(JsonArray barriers) {
    boolean status = false;
    for (JsonElement barrier : barriers) {
      JsonObject object = barrier.getAsJsonObject();
      if(object.getAsJsonPrimitive("version") != null) {
        continue;
      }

      status = true;
      object.addProperty("version", 1);

      JsonObject corner = object.getAsJsonObject("first_corner");
      String converterCorner = corner.getAsJsonPrimitive("world").getAsString() + ":" +
        ((int) corner.getAsJsonPrimitive("x").getAsDouble()) + ":" +
        ((int) corner.getAsJsonPrimitive("y").getAsDouble()) + ":" +
        ((int) corner.getAsJsonPrimitive("z").getAsDouble());

      object.addProperty("first_corner", converterCorner);

      corner = object.getAsJsonObject("second_corner");
      converterCorner = corner.getAsJsonPrimitive("world").getAsString() + ":" +
          ((int) corner.getAsJsonPrimitive("x").getAsDouble()) + ":" +
          ((int) corner.getAsJsonPrimitive("y").getAsDouble()) + ":" +
          ((int) corner.getAsJsonPrimitive("z").getAsDouble());

      object.addProperty("second_corner", converterCorner);

      String blockData = object.getAsJsonPrimitive("material").getAsString().toLowerCase();
      blockData = "minecraft:" + blockData;
      blockData += object.getAsJsonPrimitive("block_data").getAsString().toLowerCase();
      object.addProperty("block_data", blockData);
    }

    return status;
  }
}
