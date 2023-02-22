package ml.empee.mysticalbarriers;

import lombok.Getter;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.mysticalbarriers.utils.helpers.PluginItem;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Store all the existing custom items
 */

@Getter
public class PluginItems implements Bean {

  private final PluginItem selectionWand;

  public PluginItems(JavaPlugin plugin) {
    selectionWand = buildSelectionWand(plugin);
  }

  private PluginItem buildSelectionWand(JavaPlugin plugin) {
    return PluginItem.of(plugin, "barrier_wand", "1",
      ItemBuilder.from(Material.STICK)
        .setName("&eBarrier Wand")
        .setLore("\n\t&7Right click to set a barrier corner\t\n")
    );
  }

}
