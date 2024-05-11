package core.configs.server.db.nitrite.plugins.mappers.gson.converters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.dizitart.no2.collection.NitriteId;

import java.lang.reflect.Type;

public class NitriteIdConverter implements JsonSerializer<NitriteId>, JsonDeserializer<NitriteId> {
  @Override
  public NitriteId deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    if (jsonElement == null || jsonElement.isJsonNull()) {
      return null;
    }

    return NitriteId.createId(jsonElement.getAsString());
  }

  @Override
  public JsonElement serialize(NitriteId nitriteId, Type type, JsonSerializationContext jsonSerializationContext) {
    if (nitriteId == null) {
      return JsonNull.INSTANCE;
    }

    return new JsonPrimitive(nitriteId.getIdValue());
  }
}
