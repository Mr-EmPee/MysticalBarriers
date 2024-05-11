package core.configs.server.db.nitrite.plugins.mappers.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.ToNumberPolicy;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.exceptions.ObjectMappingException;
import core.configs.server.db.nitrite.plugins.mappers.gson.converters.NitriteIdConverter;

import java.util.Map;

public class GsonMapperPlugin implements NitriteMapper {

  private static final Class<Document> documentClazz = Document.class;
  private Gson gson;

  public GsonMapperPlugin(GsonBuilder gson) {
    gson.registerTypeAdapter(NitriteId.class, new NitriteIdConverter());
    gson.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE);
    gson.serializeNulls();

    this.gson = gson.create();
  }

  @Override
  public <Source, Target> Object tryConvert(Source source, Class<Target> targetClazz) {
    if (source == null) {
      return null;
    }

    JsonElement json = gson.toJsonTree(source);
    if (json.isJsonPrimitive()) {
      return extractPrimitive(json.getAsJsonPrimitive());
    }

    if (documentClazz.isAssignableFrom(targetClazz)) {
      return Document.createDocument(gson.fromJson(json, Map.class));
    }

    if (source instanceof Document) {
      return gson.fromJson(json, targetClazz);
    }

    throw new ObjectMappingException("Can't convert object to type " + targetClazz + ", try registering a gson converter for it.");
  }

  private <T> T extractPrimitive(JsonPrimitive primitive) {
    if (primitive.isBoolean()) {
      return (T) (Boolean) primitive.getAsBoolean();
    } else if (primitive.isNumber()) {
      return (T) primitive.getAsNumber();
    } else if (primitive.isString()) {
      return (T) primitive.getAsString();
    }

    return null;
  }

  @Override
  public void initialize(NitriteConfig nitriteConfig) {}

}
