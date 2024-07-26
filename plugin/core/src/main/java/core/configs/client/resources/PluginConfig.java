package core.configs.client.resources;

import io.github.empee.lightwire.annotations.LightWired;
import core.MysticalBarriers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import utils.Messenger;
import utils.files.ResourceConfig;

import java.util.List;

/**
 * The plugin configuration class.
 */

@LightWired
public class PluginConfig extends ResourceConfig {

  public PluginConfig(MysticalBarriers plugin) {
    super(plugin, "configs/config.yml", plugin.isDevelop(), List.of(
        fromV2ToV3()
    ));

    Messenger.setPrefix(getPrefix());
  }

  private static Migrator fromV2ToV3() {
    return (currentVersion, config) -> {
      if (currentVersion > 2) {
        return currentVersion;
      }

      String prefix = config.getString("messages.prefix");
      Component prefixComponent = LegacyComponentSerializer.legacy('&').deserialize(prefix);

      config.set("messages.prefix", MiniMessage.miniMessage().serialize(prefixComponent));

      return 3;
    };
  }

  public boolean blockChorusTp() {
    return getConfig().getBoolean("block-chorus-teleportation");
  }

  public boolean blockProjectiles() {
    return getConfig().getBoolean("block-movement.projectiles.enabled");
  }

  public boolean blockProjectilesOnlyFromPlayers() {
    return getConfig().getBoolean("block-movement.projectiles.only-from-player");
  }

  public boolean blockItems() {
    return getConfig().getBoolean("block-movement.items.enabled");
  }

  public boolean blockItemsOnlyFromPlayers() {
    return getConfig().getBoolean("block-movement.items.only-from-player");
  }

  public String getPrefix() {
    return getConfig().getString("messages.prefix");
  }

  public String getLang() {
    return getConfig().getString("lang");
  }

}
