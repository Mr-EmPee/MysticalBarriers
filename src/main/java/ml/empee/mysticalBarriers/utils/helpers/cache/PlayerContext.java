package ml.empee.mysticalBarriers.utils.helpers.cache;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds data for a player and invalidate them if the player goes offline
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerContext<T> {

  private static final HashMap<String, PlayerContext<?>> contexts = new HashMap<>();

  static {
    JavaPlugin plugin = JavaPlugin.getProvidingPlugin(PlayerDataRemover.class);
    plugin.getServer().getPluginManager().registerEvents(new PlayerDataRemover(), plugin);
  }

  private final HashMap<UUID, PlayerData<T>> data = new HashMap<>();

  public static <T> PlayerContext<T> get(String name) {
    return (PlayerContext<T>) contexts.computeIfAbsent(name, key -> new PlayerContext<>());
  }

  public void put(PlayerData<T> playerData) {
    data.put(playerData.getUuid(), playerData);
  }

  public void remove(Player player) {
    remove(player.getUniqueId());
  }

  public void remove(UUID uuid) {
    data.remove(uuid);
  }

  @Nullable
  public PlayerData<T> get(UUID uuid) {
    return data.get(uuid);
  }

  @Nullable
  public PlayerData<T> get(Player player) {
    return get(player.getUniqueId());
  }

  @NotNull
  public PlayerData<T> getOrPut(PlayerData<T> playerData) {
    return data.getOrDefault(playerData.getUuid(), playerData);
  }

  public Stream<PlayerData<T>> stream() {
    return data.values().stream();
  }

  private static class PlayerDataRemover implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      for (PlayerContext<?> container : contexts.values()) {
        container.data.computeIfPresent(
            player.getUniqueId(),
            (uuid, playerData) -> playerData.getPlayer() == null ? playerData : null
        );
      }
    }
  }

}
