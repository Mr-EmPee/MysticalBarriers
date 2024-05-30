package core.configs.client.resources;

import io.github.empee.lightwire.annotations.LightWired;
import core.MysticalBarriers;
import utils.Messenger;
import utils.files.ResourceConfig;

/**
 * The plugin configuration class.
 */

@LightWired
public class PluginConfig extends ResourceConfig {

  public PluginConfig(MysticalBarriers plugin) {
    super(plugin, "configs/config.yml", plugin.isDevelop(), 2);

    Messenger.setPrefix(getPrefix());
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
