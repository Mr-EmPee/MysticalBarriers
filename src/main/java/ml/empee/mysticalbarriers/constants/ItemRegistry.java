package ml.empee.mysticalbarriers.constants;

import ml.empee.itembuilder.ItemBuilder;
import ml.empee.mysticalbarriers.utils.helpers.PluginItem;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Contains all the plugin custom items
 */

public class ItemRegistry {

  private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ItemRegistry.class);
  public static final PluginItem SELECTION_WAND = new PluginItem(
      plugin, "barrier_wand", "1",
      ItemBuilder.from(Material.STICK)
          .setName("&eBarrier Wand")
          .setLore("\n\t&7Right click to set a barrier corner\t\n")
  );

}
