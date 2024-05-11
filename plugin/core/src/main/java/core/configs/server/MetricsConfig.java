package core.configs.server;

import io.github.empee.lightwire.annotations.LightWired;
import core.MysticalBarriers;
import utils.Messenger;
import utils.Metrics;

@LightWired
public class MetricsConfig {

  public MetricsConfig(MysticalBarriers plugin) {
    if (plugin.isDevelop()) {
      return;
    }

    Metrics metrics = new Metrics(plugin, 21868);
    if (metrics.isEnabled()) {
      Messenger.log("Established connection to bStats for sending metrics data");
    }
  }

}
