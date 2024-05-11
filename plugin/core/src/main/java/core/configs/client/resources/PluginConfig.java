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
    super(plugin, "configs/config.yml", plugin.isDevelop());

    Messenger.setPrefix(getPrefix());
  }

  public boolean blockChorusTp() {
    return getConfig().getBoolean("block-chorus-teleportation");
  }

  public String getPrefix() {
    return getConfig().getString("messages.prefix");
  }

  public String getLang() {
    return getConfig().getString("lang");
  }

}
