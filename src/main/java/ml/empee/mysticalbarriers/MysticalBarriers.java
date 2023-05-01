package ml.empee.mysticalbarriers;

import lombok.Getter;
import ml.empee.ioc.SimpleIoC;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.Metrics;
import ml.empee.mysticalbarriers.utils.PaperUtils;
import ml.empee.mysticalbarriers.utils.Translator;
import ml.empee.notifier.Notifier;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Boot class of this plugin.
 **/

public final class MysticalBarriers extends JavaPlugin {

  private static final String SPIGOT_PLUGIN_ID = "105671";
  private static final Integer METRICS_PLUGIN_ID = 16669;

  @Getter
  private final SimpleIoC iocContainer = new SimpleIoC(this);

  /**
   * Called when enabling the plugin
   */
  public void onEnable() {
    Translator.init(this);
    Logger.setPrefix(Translator.translate("prefix"));

    if (!PaperUtils.IS_RUNNING_PAPER) {
      printPaperWarning();
    }

    Metrics.of(this, METRICS_PLUGIN_ID);
    Notifier.listenForUpdates(this, SPIGOT_PLUGIN_ID);

    iocContainer.initialize("relocations");
  }

  private void printPaperWarning() {
    Logger.warning("THIS SERVER ISN'T RUNNING PAPER");
    Logger.warning("USE IT TO ENABLE FEATURES LIKE:");
    Logger.warning("   - Preventing living entities from getting through a barrier");
  }

  public void onDisable() {
    iocContainer.removeAllBeans(true);
  }
}
