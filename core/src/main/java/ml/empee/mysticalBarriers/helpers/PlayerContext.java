package ml.empee.mysticalBarriers.helpers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Holds data for a player and invalidate them if the player goes offline
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerContext<T> {

  private static final HashMap<String, PlayerContext<?>> REGISTERED_CONTEXTS = new HashMap<>();
  private final HashMap<UUID, T> data = new HashMap<>();

  static {
    JavaPlugin plugin = JavaPlugin.getProvidingPlugin(PlayerDataRemover.class);
    plugin.getServer().getPluginManager().registerEvents(new PlayerDataRemover(), plugin);
  }

  @NotNull
  public static <T> PlayerContext<T> get(String id) {
    return (PlayerContext<T>) REGISTERED_CONTEXTS.computeIfAbsent(id, k -> new PlayerContext<>());
  }

  public static void delete(String contextID) {
    REGISTERED_CONTEXTS.remove(contextID);
  }

  @Nullable
  public T get(Player player) {
    return data.get(player.getUniqueId());
  }

  public void remove(Player player) {
    data.remove(player.getUniqueId());
  }

  public void iterate(TriConsumer<PlayerIterator<T>, Player, T> consumer) {
    PlayerIterator<T> iterator = new PlayerIterator<>(data.entrySet().iterator());
    while(iterator.hasNext()) {
      Tuple<Player, T> entry = iterator.next();
      consumer.accept(iterator, entry.getFirstValue(), entry.getSecondValue());
    }
  }

  public void forEach(BiConsumer<Player, T> consumer) {
    data.forEach((uuid, t) -> consumer.accept(Bukkit.getPlayer(uuid), t));
  }

  public void put(Player player, T value) {
    data.put(player.getUniqueId(), value);
  }

  public T getOrPut(Player player, T defaultValue) {
    T value = data.putIfAbsent(player.getUniqueId(), defaultValue);
    if(value == null) {
      return defaultValue;
    }

    return value;
  }

  public void clear() {
    data.clear();
  }

  private static class PlayerDataRemover implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
      org.bukkit.entity.Player player = event.getPlayer();
      for(PlayerContext<?> container : REGISTERED_CONTEXTS.values()) {
        container.remove(player);
      }
    }

  }

  @RequiredArgsConstructor
  public static class PlayerIterator<T> {
    private final Iterator<Map.Entry<UUID, T>> iterator;
    private Map.Entry<UUID, T> entry;

    public void remove() {
      iterator.remove();
    }

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public Tuple<Player, T> next() {
      entry = iterator.next();
      return new Tuple<>(Bukkit.getPlayer(entry.getKey()), entry.getValue());
    }

    public void setValue(T value) {
      entry.setValue(value);
    }

  }

}
