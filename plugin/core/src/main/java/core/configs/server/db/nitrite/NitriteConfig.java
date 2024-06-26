package core.configs.server.db.nitrite;

import core.configs.server.db.nitrite.migrations.SchemaUpdateV2;
import core.model.Barrier;
import io.github.empee.lightwire.annotations.LightWired;
import org.bukkit.plugin.java.JavaPlugin;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.module.NitriteModule;
import org.dizitart.no2.migration.InstructionSet;
import org.dizitart.no2.migration.Migration;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import core.configs.server.db.nitrite.plugins.mappers.gson.GsonMapperPlugin;
import utils.Messenger;
import utils.converters.BukkitGson;

import java.io.Closeable;
import java.io.File;


/**
 * Configuration of nitrite db
 */

@LightWired
public class NitriteConfig implements Closeable {

  private final Nitrite database;

  public NitriteConfig(JavaPlugin plugin) {
    Messenger.log("Connecting to the database...");

    var persistentStorage = MVStoreModule.withConfig()
        .filePath(new File(plugin.getDataFolder(), "database.nitrite"))
        .build();

    var mappingModule = NitriteModule.module(new GsonMapperPlugin(BukkitGson.gson()));

    this.database = Nitrite.builder()
        .schemaVersion(2)
        .addMigrations(new SchemaUpdateV2())
        .loadModule(persistentStorage)
        .loadModule(mappingModule)
        .openOrCreate();
  }

  public <T> ObjectRepository<T> getRepository(Class<T> clazz) {
    return database.getRepository(clazz);
  }

  public void close() {
    Messenger.log("Closing database connections...");
    database.close();
  }
}
