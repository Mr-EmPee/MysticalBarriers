package ml.empee.mysticalBarriers.utils.helpers.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerData<T> {

  @SafeVarargs
  public static <T> PlayerData<T> of(@NotNull Player player, T... data) {
    PlayerData<T> playerData = new PlayerData<>(player.getUniqueId(), player);
    playerData.set(data);
    return playerData;
  }

  @SafeVarargs
  public static <T> PlayerData<T> of(@NotNull UUID uuid, T... data) {
    PlayerData<T> playerData = new PlayerData<>(uuid, null);
    playerData.set(data);
    return playerData;
  }

  @Getter
  private final UUID uuid;
  @Getter @Nullable
  private final Player player;
  @Delegate
  private final List<T> data = new ArrayList<>();

  public T get() {
    if (data.isEmpty()) {
      return null;
    }

    return data.get(0);
  }

  @SafeVarargs
  public final void set(T... data) {
    clear();

    if(data != null) {
      addAll(Arrays.asList(data));
    }
  }

}
